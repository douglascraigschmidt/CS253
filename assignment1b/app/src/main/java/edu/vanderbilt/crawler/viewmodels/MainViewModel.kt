package edu.vanderbilt.crawler.viewmodels

import android.app.Application
import androidx.annotation.MainThread
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import edu.vanderbilt.crawler.platform.AndroidCache
import edu.vanderbilt.crawler.utils.KtLogger
import edu.vanderbilt.crawler.utils.debug
import edu.vanderbilt.crawler.utils.info
import edu.vanderbilt.crawler.utils.warn
import edu.vanderbilt.crawler.viewmodels.MainViewModel.CrawlState.*
import edu.vanderbilt.imagecrawler.crawlers.CrawlerType
import edu.vanderbilt.imagecrawler.crawlers.ImageCrawler
import edu.vanderbilt.imagecrawler.platform.Cache
import edu.vanderbilt.imagecrawler.platform.Controller
import edu.vanderbilt.imagecrawler.utils.Options
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.yield
import java.util.concurrent.ConcurrentHashMap
import kotlin.properties.Delegates.observable
import kotlin.system.measureTimeMillis

class MainViewModel(app: Application) : BaseViewModel(app), Cache.Observer, KtLogger {
    // Crawl state posted as CrawlProgress
    enum class CrawlState {
        IDLE,
        RUNNING,
        CANCELLING,
        CANCELLED,
        COMPLETED,
        FAILED
    }

    data class CrawlProgress(val state: CrawlState,
                             val threads: Int = 0,
                             val millisecs: Long = 0L)

    /** Current crawl state */
    var state by observable(IDLE) { _, old, new ->
        if (old != new) {
            crawlProgressFeed.postValue(CrawlProgress(new, threads.size))
            debug("MainViewModel state change: $old -> $new")
        } else {
            debug("MainViewModel state is already $new (not posting value to activity)")
        }
    }

    val isCrawlRunning: Boolean
        get() = state != IDLE && state != COMPLETED && state != CANCELLED

    // Delegate crawl speed to cache field.
    var crawlSpeed
        get() = AndroidCache.crawlSpeed
        set(speed) {
            AndroidCache.crawlSpeed = speed
        }

    val isRunning: Boolean
        get() = when (state) {
            RUNNING, CANCELLING -> true
            IDLE, CANCELLED, COMPLETED, FAILED -> false
        }

    /**
     * Crawler support properties.
     */
    private var crawler: ImageCrawler? = null

    /** Cache events live data feed. */
    private val cacheContentsFeed = MutableLiveData<List<Resource>>()

    /** Crawl state live data feed. */
    private var crawlProgressFeed = MutableLiveData<CrawlProgress>()

    /**
     * Map containing an entry for each running thread. The entry values
     * will be [1, threads.size] and each value is used to identify the
     * thread in the UI grid view.
     */
    private var threads = ConcurrentHashMap<Long, Int>()

    /** Support collection for cached resources live data feed. */
    private var cacheMap = ConcurrentHashMap<Cache.Item, Resource>()

    /**
     * Called to subscribe to live data. Sets the live data observer and
     * also adds this calls as a cache watcher. Since the call to startWatching
     * immediately calls back the observer with each of the existing cached
     * items, the call is performed in a background thread so that the call
     * will not block the UI thread.
     */
    fun subscribe(lifecycleOwner: LifecycleOwner,
                  cacheObserver: Observer<List<Resource>>,
                  crawlObserver: Observer<CrawlProgress>,
                  block: () -> Unit) {
        cacheContentsFeed.observe(lifecycleOwner, cacheObserver)
        crawlProgressFeed.observe(lifecycleOwner, crawlObserver)

        // Normally we would want the startWatching call to automatically
        // post back the current cache contents (true for 2nd parameter)
        // but because the image-crawler has a brain-dead synchronized
        // CacheMap implementation (for Student illustration), this posting
        // operation will block and starve this main thread if the simulator
        // is set to a long artificial delay. To work around this issue,
        // call the startWatching method in a back ground thread so that
        // the UI doesn't block.
        // NOTE: this would not be necessary if the cache only used a
        // ConcurrentHashMap.
        launch {
            // If crawl is running, there is no need to post the cache
            // contents back to the main thread since that will already
            // be happening from the currently running crawl.
            val notifyCurrentContents = state != RUNNING
            AndroidCache.startWatching(this@MainViewModel, notifyCurrentContents)
            check(!Thread.currentThread().isInterrupted) {
                "Uh oh"
            }
        }

        block()
    }

    /**
     * Unsubscribe lifecycle [owner] from all subscribed feeds set by [subscribe].
     */
    fun unsubscribe(owner: LifecycleOwner) {
        cacheContentsFeed.removeObservers(owner)
        crawlProgressFeed.removeObservers(owner)
    }

    /**
     * Unsubscribe a previously subscribed * crawl progress [observer].
     */
    fun unsubscribeCacheObserver(observer: Observer<List<Resource>>) {
        cacheContentsFeed.removeObserver(observer)
    }

    /**
     * Unsubscribe a previously subscribed cache [observer].
     */
    fun unsubscribeCrawlObserver(observer: Observer<CrawlProgress>) {
        crawlProgressFeed.removeObserver(observer)
    }

    /**
     * Non-blocking call that begins a crawl. To prevent any edge
     * cases, this function and cancelCrawl must be called from
     * main thread to ensure that these methods can only be called
     * in the correct sequence.
     */
    @MainThread
    fun startCrawl(strategy: CrawlerType, controller: Controller) {
        // If previous crawl is being cancelled then wait for it to complete.
        if (state == CANCELLING) {
            runBlocking {
                while (state == CANCELLING) {
                    yield()
                }
                check(state != CANCELLING)
            }
        }

        check(state != CANCELLING)

        check(state == IDLE || state == COMPLETED || state == CANCELLED) {
            "startCrawl() called when a crawl is $state"
        }

        // Must be set in main thread
        state = RUNNING

        launch {
            check(!Thread.currentThread().isInterrupted) {
                "MainViewModel.startCrawl: thread " +
                        "[${Thread.currentThread().id}] has interrupted flag set"
            }

            // Clear the mutable live data instance, cache, and
            // local hashMap.
            clearAll()

            // Build a new crawler using the specified strategy.

            Options.mDebug = true
            crawler = ImageCrawler.Factory.newCrawler(strategy, controller)

            measureTimeMillis {
                state = crawl()
            }
        }
    }

    /**
     * Non-blocking call that begins a crawl cancellation sequence.
     * Must be called from the main thread (see [startCrawl] for a
     * detailed explanation).
     * @return current state
     */
    @MainThread
    fun cancelCrawl(): CrawlState {
        check(state != CANCELLING) {
            "cancelCrawl() called when a crawl is already cancelling"
        }

        if (state == RUNNING) {
            state = CANCELLING
            crawler?.stopCrawl()
        }

        return state
    }

    /**
     * Runs a crawl and returns once the crawl has completed,
     * was cancelled, or terminated with an exception.
     */
    private fun crawl(): CrawlState {
        check(state == RUNNING) {
            "crawl() expected crawl state to be RUNNING not $state"
        }


        try {
            crawler?.run() ?: info("Crawler was not started.")
            info("Crawler finished normally.")
        } catch (t: Throwable) {
            warn("Crawler finished with exception: $t")
        } finally {
            // Always clear interrupted flag so that next crawl will not abort.
            if (Thread.currentThread().isInterrupted) {
                Thread.interrupted()
            }

            check(state == RUNNING || state == CANCELLING) {
                "Crawl state ($state) should be RUNNING or CANCELLING after crawler.run() completes"
            }

            crawler = null
            val endState = if (state == CANCELLING) CANCELLED else COMPLETED
            postFinalCrawlResultsList(endState)
            return endState
        }
    }

    /**
     * Mutable live data must be cleared on the main thread
     * when using synchronous value property. The call to
     * clear the mutable live data will be propagated to the
     * app layer which will clear the dependent UI elements
     * so there is no need to rely on event callbacks from
     * the cache as items are deleted.
     */
    @MainThread
    fun clearAll() {
        if (state == RUNNING || state == CANCELLING) {
            warn("Unable to clear items when crawl is running or being cancelled.")
            return
        }

        state = IDLE

        AndroidCache.stopWatching(this)
        AndroidCache.clear()
        cacheMap.clear()
        threads.clear()
        postCacheContents()

        //TODOx: fix issue and then change to check
        if (cacheMap.size != 0) {
            warn("HashMap.clear() results in size of ${cacheMap.size}")
        }
        //TODOx: fix issue and then change to check
        if (threads.size != 0) {
            warn("threads.clear() results in size of ${threads.size}")
        }

        AndroidCache.startWatching(this, false)
    }

    fun delete(list: List<Resource>) {
        if (state == RUNNING || state == CANCELLING) {
            warn("Unable to delete items when crawl is running or being cancelled.")
            return
        }

        state = IDLE

        list.forEach { resource ->
            // Edge case: clear item from local cache in case the item
            // is not in the AndroidCache (in which case remove() will not
            // end up posting a delete event to cache observers).
            AndroidCache.remove(resource.url)
            cacheMap.entries.firstOrNull { entry ->
                entry.value.url == resource.url
            }?.let {
                event(Cache.Operation.DELETE, it.key, -1f)
            }
        }

        //postCacheContents()

        check(state == IDLE) {
            "State should be IDLE after deleting items but state is $state."
        }
    }

    /**
     * Helper method that updates all incomplete cached resources
     * to cached resource list to
     * reflect completion state and forwards this information
     * to the cache contents feed.
     */
    private fun postFinalCrawlResultsList(endState: CrawlState) {
        // When a crawl is cancelled Update any cached entries to have a CLOSED state to signal
        // that UI that they should consider these values all finished
        synchronized(cacheMap) {
            cacheMap.forEach { (key, value) ->
                when (value.state) {
                    Resource.State.LOAD,
                    Resource.State.CLOSE -> {
                        // Do nothing since resource completed.
                    }
                    else -> {
                        if (endState == CANCELLED) {
                            cacheMap[key] = value.copy(state = Resource.State.CANCEL)
                        }
                    }
                }
            }
            val mutableList = cacheMap.values.toMutableList()
            mutableList.sortBy { it.timestamp }
            cacheContentsFeed.postValue(mutableList)
        }
    }

    /**
     * Called by all crawler threads to update the resource list to reflect
     * this event change and posts the updated list to application via the
     * Android Architecture LiveData posts.
     *
     * To reduce unnecessary traffic, the progress value converted to an
     * integer percent value and the live data is only updated if the
     * operation has changed or if the operation has not changed but the
     * progress has changed.
     *
     * Each received event's progress is used to create a Resource value that is
     * sent to the view to display. Resources are saved in a map so that each time
     * a new event is processed, only the current events resource is updated in
     * the hash map, and the entire map is converted to an immutable list and
     * forwarded to the view to display via live data. This list is, effectively,
     * a state object that represents the entire current state of the crawl.
     */
    override fun event(operation: Cache.Operation, item: Cache.Item, progress: Float) {
        if (state == CANCELLING) {
            // Always clear any possible set thread interrupted flag.
            Thread.interrupted()

            // Now throw and exception to force a quick termination of this thread.
            throw InterruptedException("Crawl has been cancelled")
        }

        if (operation == Cache.Operation.DELETE) {
            // There's and edge case where an item may be in the recycler view
            // but not have a hashMap entry due. To ensure that the hashMap and
            // recycler view adapter do not get out of sync, always fall through
            // here and allow the code at the end of this method to run and
            // refresh the adapter list from the cash.
            cacheMap.remove(item)
        } else {
            // Add the thread to the map if not already there and set it's value to
            // the size of the map. This value is displayed in the UI to identify
            // which threads are being use to process each image (in the grid view).
            val threadId =
                    threads.getOrPut(Thread.currentThread().id) {
                        (threads.size + 1).also {
                            crawlProgressFeed.postValue(CrawlProgress(state, it))
                        }
                    }

            // Map the operation and item to an application resource.
            val resource = Resource.fromFileObserver(item,
                    operation,
                    (progress * 100f).toInt(),
                    threadId)

            // Only add resource if the map doesn't
            // already contain a resource for this item.
            val oldResource = cacheMap.putIfAbsent(item, resource)

            // Check if item key was already in the hashMap.
            oldResource?.let {
                // If the old and new resources are the same then there is no
                // need to update the UI layer. If the item's resource value
                // has changed, then attempt to replace the item value. This may
                // fail if another thread updated the resource value between
                // the putIfAbsent() call and the following replace() call. In
                // this case, that thread will update the UI layer and this
                // thread can simply return.
                if (oldResource == resource || !cacheMap.replace(item, it, resource)) {
                    return
                }
            }
        }

        postCacheContents()
    }

    private fun postCacheContents() {
        // Update the UI layer by converting the hashMap to a sorted list that
        // is then posted to the LiveData feed.
        val mutableList = cacheMap.values.toMutableList()

        // The posted list is sorted by item creation timestamps so that the UI
        // layer will consistently show each item in the same logical time ordered
        // position after each live data update.
        mutableList.sortBy { it.timestamp }

        // Post the sorted list as an immutable list. The live data feed is typed
        // to MutableLiveData<List<Resource>> so the passed list will be received
        // as immutable.
        cacheContentsFeed.postValue(mutableList)
    }
}

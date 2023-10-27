package edu.vanderbilt.crawler.adapters

import android.content.Context
import android.graphics.BlendMode
import android.graphics.BlendModeColorFilter
import android.graphics.Color
import android.graphics.PorterDuff.Mode
import android.graphics.drawable.Drawable
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.recyclerview.widget.DiffUtil
import edu.vanderbilt.crawler.R
import edu.vanderbilt.crawler.extensions.asyncLoad
import edu.vanderbilt.crawler.extensions.asyncLoadGif
import edu.vanderbilt.crawler.extensions.clear
import edu.vanderbilt.crawler.extensions.minmax
import edu.vanderbilt.crawler.ui.screens.pager.PagedAdapterClient
import edu.vanderbilt.crawler.ui.screens.settings.Settings
import edu.vanderbilt.crawler.utils.KtLogger
import edu.vanderbilt.crawler.viewmodels.Resource
import edu.vanderbilt.crawler.viewmodels.Resource.State.*
import java.io.File
import kotlin.math.ceil

/**
 * Adapter that displays an image URL and its associated image.
 */
class ImageViewAdapter(context: Context,
                       list: MutableList<Resource> = mutableListOf(),
                       val gridLayout: Boolean = false,
                       onSelectionListener: OnSelectionListener? = null)
    : MultiSelectAdapter<Resource, ImageViewAdapter.Holder>(
        context, list, onSelectionListener),
        KtLogger,
        PagedAdapterClient {

    /**
     * PagedAdapterClient callback that is required to return a list
     * containing each item's image uri. Note that this list
     * is assumed to have the same ordering as the items appear
     * in the recycler view.
     */
    override fun getImageUris(): List<Uri> {
        // Map all resources to uris for PagedActivity
        return items.map { Uri.parse(File(it.filePath!!).toURI().toString()) }
    }

    companion object {
        const val KEY_PROGRESS_VALUE = "key_progress_value"
        const val KEY_SIZE = "key_size"
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): Holder {
        val view = LayoutInflater.from(parent.context).inflate(
                if (gridLayout) R.layout.image_grid_item else R.layout.image_list_item,
                parent,
                false)
        return Holder(view)
    }

    override fun onBindViewHolder(holder: Holder, position: Int) {
        holder.bind(getItem(position))
        super.onBindViewHolder(holder, position)
    }

    override fun onBindViewHolder(holder: Holder,
                                  position: Int,
                                  payloads: MutableList<Any>) {
        if (payloads.isNotEmpty()) {
            // Local DiffUtils implementation puts a bundle at index 0.
            with(payloads[0] as Bundle) {
                val resource = items[position]
                keySet().forEach { key ->
                    when (key) {
                        KEY_PROGRESS_VALUE -> holder.setProgress(resource)
                        KEY_SIZE -> holder.sizeView.text =
                                holder.getSize(resource)
                        else -> {
                            throw IllegalStateException(
                                    "Unsupported DiffUtils key value: $key")
                        }
                    }
                }
            }

            // Assumption: at least 1 key will match so
            // don't call default handler.
            return
        }

        // Call page adapter to set unique ImageView transition name
        // for the ImageView contained in this holder.
        onBindPagedAdapterViewHolder(holder)

        super.onBindViewHolder(holder, position, payloads)
    }

    override fun onViewRecycled(holder: Holder) {
        holder.onViewRecycled()
        super.onViewRecycled(holder)
    }

    /**
     * Updates the state of an existing item.
     */
    override fun replaceItem(position: Int, item: Resource) {
        val oldItem = getItem(position)
        validateStateChange(oldItem.state, item.state)

        if (!DiffUtilCallback.areItemsTheSame(oldItem, item)) {
            throw Exception("Replace items must be logically equal")
        }

        if (!DiffUtilCallback.areContentsTheSame(oldItem, item)) {
            super.replaceItem(position, item)
        }
    }

    fun updateItems(newItems: List<Resource>) {
        val oldItems = items
        val result = DiffUtil.calculateDiff(
                DiffUtilCallback(oldItems, newItems))
        setItems(newItems, false)
        result.dispatchUpdatesTo(this)
    }

    /**
     * State change verification. Expected transitions are:
     *   [CREATE|LOAD] -> [READ|WRITE|PROCESS|DOWNLOAD] -> CLOSE
     */
    private fun validateStateChange(oldState: Resource.State,
                                    newState: Resource.State): Boolean {
        when (newState) {
            CREATE, LOAD ->
                error("Illegal state change " +
                        "${oldState.name} -> ${newState.name}")
            PROCESS,
            READ,
            WRITE,
            DOWNLOAD ->
                if (oldState != CREATE) {
                    error("Illegal state change " +
                            "${oldState.name} -> ${newState.name}")
                }
            CLOSE,
            CANCEL -> {
            }
        }

        return true
    }

    /**
     * Sets all items to CLOSE state so that transient
     * gif animations are stopped.
     */
    fun crawlStopped(cancelled: Boolean = false) {
        items.forEachIndexed { pos, item ->
            when (item.state) {
                DOWNLOAD, CREATE, READ, WRITE, PROCESS, CANCEL -> {
                    val state = if (cancelled) CANCEL else CLOSE
                    super.replaceItem(pos, item.copy(state = state))
                }
                LOAD, CLOSE -> {
                }
            }
        }
    }

    fun findItem(url: String): Int {
        val item = items.find { it.url == url }
        return if (item == null) -1 else items.indexOf(item)
    }

    fun remove(url: String) {
        items.find { it.url == url }?.also {
            remove(items.indexOf(it))
        }
    }

    /**
     * Inner view holder implementation that also implements
     * PagedAdapterViewHolder to set a unique transition name
     * for each holder image view.
     *
     * Note that there are too many controls to use a single
     * "when(state)" block to update all the views. It was
     * a clearer design to have each view have it's own
     * "when(state)" block.
     */
    inner class Holder(val view: View)
        : SelectableViewHolder(view) {

        private lateinit var url: String
        private val textView: TextView? = if (gridLayout) null else view.findViewById(R.id.textView)
        private val threadView = view.findViewById<TextView>(R.id.thread)
        private val imageView = view.findViewById<ImageView>(R.id.imageView)
        internal val sizeView = view.findViewById<TextView>(R.id.imageSize)
        private val progressBar = view.findViewById<ProgressBar>(R.id.progressBar)
        private val stateView = view.findViewById<TextView>(R.id.state)

        fun bind(resource: Resource) {
            url = resource.url
            textView?.text = url
            sizeView.text = getSize(resource)
            threadView.text = getThreadId(resource)
            sizeView.setTextColor(
                    if (resource.state == CANCEL)
                        Color.RED
                    else {
                        ContextCompat.getColor(view.context, R.color.secondaryTextColor)
                    })
            stateView.text = resource.state.name
            setImage(resource)
            setProgress(resource)

            // Start off with all views that are enabled in settings
            // to reflect that state. They will be hidden based on
            // the current state of this widget.
            showState(true)
            showSize()
            showThread(true)

            when (resource.state) {
                DOWNLOAD -> setProgress(resource.progress, Color.RED)
                WRITE -> setProgress(resource.progress, Color.BLUE)
                READ -> setProgress(resource.progress, Color.GREEN)
                PROCESS -> setProgress(resource.progress, Color.CYAN)
                LOAD, CREATE, CLOSE, CANCEL -> {
                    handleCompletedState()
                }
            }
        }

        fun onViewRecycled() {
            imageView.clear()
        }

        fun setProgress(resource: Resource) {
            progressBar.max = 100
            when (resource.state) {
                DOWNLOAD -> setProgress(resource.progress, Color.RED)
                WRITE -> setProgress(resource.progress, Color.BLUE)
                READ -> setProgress(resource.progress, Color.GREEN)
                PROCESS -> setProgress(resource.progress, Color.CYAN)
                LOAD, CREATE, CLOSE, CANCEL -> showProgress(false)
            }
        }

        internal fun getSize(resource: Resource): String {
            return if (resource.size != 0) {
                roundSizeToNearestK(view.context, resource.size)
            } else {
                when (resource.state) {
                    READ -> "reading"
                    DOWNLOAD -> "downloading"
                    PROCESS -> "transforming"
                    LOAD, CLOSE, CANCEL -> {
                        resource.filePath?.let {
                            roundSizeToNearestK(view.context, File(it).length().toInt())
                        } ?: roundSizeToNearestK(view.context, 0)
                    }
                    else -> roundSizeToNearestK(view.context, 0)
                }
            }
        }

        private fun roundSizeToNearestK(context: Context, size: Int): String {
            return if (size == 0) {
                context.getString(R.string.image_size_format, 0f)
            } else {
                context.getString(
                        R.string.image_size_format,
                        ceil(size.toFloat() / 1e3))
            }
        }

        private fun getThreadId(resource: Resource): String {
            return when (resource.state) {
                // TODOx:monte - for Doug to see threads
                // LOAD, CLOSE, CANCEL -> ""

                else -> "thread: ${resource.thread}"
            }
        }

        private fun setProgress(progress: Int, color: Int) {
            with(progressBar) {
                progressDrawable.setPorterDuffSrcInColorFilter(color)
                this.progress = (progress * max / 100f).toInt().minmax(0, 100)
                showProgress(this.progress < this.max)
            }
        }

        private fun Drawable.setPorterDuffSrcInColorFilter(color: Int) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                colorFilter = BlendModeColorFilter(color, BlendMode.SRC_IN)
            } else {
                @Suppress("DEPRECATION")
                setColorFilter(color, Mode.SRC_IN)
            }
        }

        private fun setImage(resource: Resource) {
            imageView.run {
                imageView.clear()
                when (resource.state) {
                    DOWNLOAD -> asyncLoadGif(R.drawable.down_arrow)
                    CREATE -> asyncLoad(R.drawable.placeholder)
                    READ,
                    WRITE -> asyncLoadGif(R.drawable.spinning_cdrom)
                    PROCESS -> asyncLoadGif(R.drawable.filter)
                    LOAD,
                    CANCEL,
                    CLOSE -> {
                        if (!resource.filePath.isNullOrEmpty()) {
                            val file = File(resource.filePath)
                            if (file.isFile && file.length() > 0) {
                                asyncLoad(file.toURI().toString())
                            } else {
                                asyncLoad(R.drawable.error)
                            }
                        } else {
                            asyncLoad(R.drawable.error)
                        }

                        showProgress(false)
                    }
                }
            }
        }

        /**
         * Allows periodically hiding of the view if it has
         * been enabled in Settings but never shows the view
         * if it has been turned of in the settings.
         */
        private fun showProgress(show: Boolean) {
            if (Settings.showProgress || !show) {
                progressBar.isVisible = show
            }
        }

        /**
         * Allows periodically hiding of the view if it has
         * been enabled in Settings but never shows the view
         * if it has been turned of in the settings.
         */
        private fun showThread(show: Boolean) {
            if (Settings.showThread || !show) {
                threadView.isVisible = show
            }
        }

        /**
         * Size is a special case: when enabled in settings,
         * always show the size, i.e., it is never periodically
         * hidden during a crawl.
         */
        private fun showSize() {
            sizeView.isVisible = Settings.showSize
        }

        /**
         * Allows periodically hiding of the view if it has
         * been enabled in Settings but never shows the view
         * if it has been turned of in the settings.
         */
        private fun showState(show: Boolean) {
            if (Settings.showState || !show) {
                stateView.isVisible = show
            }
        }

        /**
         * Always hide transient views and only hide size
         * view if user has turned it off in settings.
         */
        private fun handleCompletedState() {
            showState(false)
            // TODOx:monte - for Doug to see threads
            if (!Settings.showThread) {
                showThread(false)
            }
            showProgress(false)
            showSize()
        }

        override fun toString(): String {
            return super.toString() + " '$url'"
        }
    }

    /**
     * Compares items so that the least amount of refreshing
     * is performed during item updates. Currently, due to
     * the user's ability to turn state, progress, size, and
     * thread info views on and off (dynamically), this class
     * always assumes that an items contents are different
     * which causes more redraws, but also enables a nicer layout
     * when these 4 views are toggled on and off as they are
     * needed and not needed (progress, size, and state are
     * toggled on and off depending on the items resource
     * state, the thread always stays on if enabled in settings).
     * Once the crawl has completed, the transient views (state,
     * progress, and thread) are always hidden. Only size will
     * remain (if enabled in settings).
     */
    class DiffUtilCallback(private val oldList: List<Resource>,
                           private val newList: List<Resource>)
        : DiffUtil.Callback() {

        /**
         *
         */
        companion object {
            @Suppress("UNUSED_PARAMETER")
            fun areContentsTheSame(oldItem: Resource, newItem: Resource): Boolean {
                // Always return false to force grid layout rows to grow and shrink
                // as transient views are shown and hidden. If true is returned here,
                // the activity has to force a notifyDataSetChanged at the end of a
                // crawl in order to have each item shrink to reflect the "gone"
                // state of any of their transient widgets.
                return false
                /*
                // We only care if the properties that map to
                // visual components are the same/different.
                return oldItem.state == newItem.state
                       && oldItem.size == newItem.size
                       && oldItem.progress == newItem.progress
                */
            }

            fun areItemsTheSame(oldItem: Resource, newItem: Resource) =
                    // Only care if items are the same not their contents.
                    oldItem.url == newItem.url &&
                            oldItem.filePath == newItem.filePath
        }

        override fun getOldListSize(): Int = oldList.size

        override fun getNewListSize(): Int = newList.size

        override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int) =
                areItemsTheSame(oldList[oldItemPosition], newList[newItemPosition])

        override fun areContentsTheSame(oldItemPosition: Int, newItemPosition: Int) =
                areContentsTheSame(oldList[oldItemPosition], newList[newItemPosition])

        override fun getChangePayload(oldItemPosition: Int, newItemPosition: Int): Any? {
            val oldItem = oldList[oldItemPosition]
            val newItem = newList[newItemPosition]

            if (oldItem.state == newItem.state) {
                val bundle = Bundle()
                if (oldItem.progress != newItem.progress) {
                    bundle.putParcelable(KEY_PROGRESS_VALUE, newItem)
                }
                if (oldItem.size != newItem.size) {
                    bundle.putParcelable(KEY_SIZE, newItem)
                }

                // Only return bundle if something was added.
                return if (!bundle.isEmpty) bundle else null
            }

            return null
        }
    }
}

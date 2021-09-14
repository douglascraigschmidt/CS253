package edu.vanderbilt.crawler.ui.screens.settings

import android.content.SharedPreferences
import android.os.Bundle
import android.view.ContextThemeWrapper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.google.android.material.bottomsheet.BottomSheetBehavior.*
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.jakewharton.rxbinding2.widget.RxAdapterView
import com.jakewharton.rxbinding2.widget.RxCompoundButton
import com.jakewharton.rxbinding2.widget.RxRadioGroup
import com.jakewharton.rxbinding2.widget.RxSeekBar
import edu.vanderbilt.crawler.R
import edu.vanderbilt.crawler.databinding.SettingsDialogFragmentBinding
import edu.vanderbilt.crawler.extensions.enable
import edu.vanderbilt.crawler.extensions.grabTouchEvents
import edu.vanderbilt.crawler.preferences.CompositeUnsubscriber
import edu.vanderbilt.crawler.preferences.ObservablePreference
import edu.vanderbilt.crawler.preferences.PreferenceProvider
import edu.vanderbilt.crawler.preferences.Subscriber
import edu.vanderbilt.crawler.ui.screens.settings.Settings.GRID_SCALE_RANGE
import edu.vanderbilt.crawler.ui.screens.settings.Settings.TRANSPARENCY_RANGE
import edu.vanderbilt.crawler.ui.screens.settings.adapters.CrawlDepthAdapter
import edu.vanderbilt.crawler.ui.screens.settings.adapters.CrawlerTypeSpinnerAdapter
import edu.vanderbilt.crawler.ui.screens.settings.adapters.TransformsAdapter
import edu.vanderbilt.imagecrawler.crawlers.CrawlerType
import io.reactivex.disposables.CompositeDisposable

/**
 * Application developer options fragment that shows a list of tunable
 * options in a modal bottom sheet.
 *
 * Note that any SeekBars added to this settings panel should call
 * [SeekBar.grabTouchEvents] so that sliding the thumb button to
 * right will not be interpreted by the panel as a slide closed
 * action.
 *
 * To show this bottom sheet:
 * <pre>
 * SettingsDialogFragment.newInstance().show(getSupportFragmentManager(), "dialog");
 * </pre>
 * You activity (or fragment) needs to implement [SettingsDialogFragment.Listener].
 */
class SettingsDialogFragment :
    BottomSheetDialogFragment(),
    SharedPreferences.OnSharedPreferenceChangeListener {

    companion object {
        fun newInstance(): SettingsDialogFragment {
            return SettingsDialogFragment()
        }
    }

    /**
     * Temporary flag set when a user action triggers one of the
     * widget Observables. When this happens, the shared preference
     * listener will ignore any notification received and reset the
     * flag to false.
     */
    private var userAction = false

    /**
     * Observables should always be disposed. In particular, they
     * need to be disposed when the user clicks the reset button
     * which reconfigures all widgets.
     */
    private var compositeDisposable = CompositeDisposable()

    /**
     * Flag indicating if a crawler is running. Used to
     * prevent reset of critical model parameters while a
     * crawler is running.
     */
    private var crawlerRunning = false

    private lateinit var binding: SettingsDialogFragmentBinding

    /** RxJava subscriptions */
    private val disposables = CompositeDisposable()

    /** Observe all crawl speed preference changes. */
    private val compositeUnsubscriber = CompositeUnsubscriber()

    private var crawlSpeed: Int by ObservablePreference(
        default = 100,
        name = "CrawlSpeedPreference",
        subscriber = object : Subscriber<Int> {
            override val subscriber: (Int) -> Unit
                get() = { binding.speedSeekBar.progress = it }

            override fun unsubscribe(callback: () -> Unit) {
                compositeUnsubscriber.add(callback)
            }
        })

    private var speedBarState: Int by ObservablePreference(
        default = STATE_COLLAPSED,
        name = "speedBarStatePreference",
        subscriber = object : Subscriber<Int> {
            override val subscriber: (Int) -> Unit
                get() = { binding.settingsShowSpeedBar.isChecked = it != STATE_HIDDEN }

            override fun unsubscribe(callback: () -> Unit) {
                compositeUnsubscriber.add(callback)
            }
        })

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // create ContextThemeWrapper from the original Activity Context with the custom theme
        val contextThemeWrapper = ContextThemeWrapper(activity, R.style.SettingsThemeDark)

        // clone the inflater using the ContextThemeWrapper
        val localInflater = inflater.cloneInContext(contextThemeWrapper)
        binding = SettingsDialogFragmentBinding.inflate(localInflater, container, false)
        return binding.root
    }

    override fun onDestroyView() {
        compositeUnsubscriber.invoke()
        disposables.clear()
        super.onDestroyView()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        configureWidgets()
        PreferenceProvider.addListener(this)
    }

    /**
     * Configures all widgets and install Rx Widget Observers.
     */
    private fun configureWidgets() {
        configureTransforms()
        configureCrawlerStrategy()
        configureCrawlerMaxDepth()
        configureCrawlerLocation()
        configureDebugLogging()
        configureThreadSpeed()
        configureShowSpeedBar()
        configureViewScale()
        configureViewTransparency()
        configureShowProgress()
        configureShowState()
        configureShowSize()
        configureShowThread()
        configureTransformsLocation()
        configureResetToDefaults()
        configureSaveOnExit()
    }

    private fun configureTransforms() {
        // Let the adapter handle adding child views.
        TransformsAdapter.buildAdapter(binding.transformsLayoutView)
    }

    /**
     * Resets all Settings shared preferences to their default
     * values. This is tricky because the simplest code to handle
     * this is to call each widgets configure method. This requires
     * uninstalling all RxObservers which are then reinstalled
     * in each configuration function. The problem is that if a
     * shared preference hasn't changed, then the SharedPreference
     * manager will not send a shared preference change event.
     * Therefore, we can't rely on the installed shared preference
     * listener as the main mechanism to handle calling all the
     * configuration functions. Instead, the configureWidgets
     * function is called via the updatePreference wrapper function
     * which ensures that no shared preference processing is called
     * while the configuration is running.
     */
    @Suppress("UNUSED_PARAMETER")
    private fun configureResetToDefaults(unused: Boolean = true) {
        with(binding) {
            settingsResetToDefaults.setOnClickListener {
                updatePreference {
                    Settings.resetToDefaults(crawlerRunning)
                    compositeDisposable.dispose()
                    compositeDisposable = CompositeDisposable()
                    configureWidgets()
                }
            }
        }
    }

    private fun configureCrawlerMaxDepth() {
        // Setup adapter and current selection.
        val adapter = CrawlDepthAdapter(
            ContextThemeWrapper(activity, R.style.SettingsThemeDark)
        )
        with(binding) {
            settingsCrawlDepth.adapter = adapter
            settingsCrawlDepth.setSelection(
                CrawlDepthAdapter.getPositionForValue(Settings.crawlDepth)
            )

            // Use Rx to filter item selections and save changed value to shared preference.
            RxAdapterView.itemSelections(settingsCrawlDepth)
                .skipInitialValue()
                .map<Int> { adapter.getItem(it) }
                .filter { it != Settings.crawlDepth }
                .subscribe { Settings.crawlDepth = it }
                .also { disposables.add(it) }
        }
    }

    private fun configureCrawlerStrategy() {
        // Setup adapter and current selection.
        val adapter = CrawlerTypeSpinnerAdapter(
            ContextThemeWrapper(activity, R.style.SettingsThemeDark),
            CrawlerType::class.java
        )
        with(binding) {
            settingsCrawlStrategy.adapter = adapter
            settingsCrawlStrategy.setSelection(
                adapter.getPositionForValue(Settings.crawlStrategy) ?: 0
            )

            // Use Rx to filter item selections and save changed value to shared preference.
            RxAdapterView.itemSelections(settingsCrawlStrategy)
                .skipInitialValue()
                .map<CrawlerType> { adapter.getItem(it) }
                .filter { it != Settings.crawlStrategy }
                .subscribe { Settings.crawlStrategy = it }
                .also { disposables.add(it) }
        }
    }

    private fun configureCrawlerLocation() {
        with(binding) {
            val checkedId = if (Settings.localCrawl) {
                settingsLocalCrawl.id
            } else {
                settingsRemoteCrawl.id
            }
            settingsImageSourceRadioGroup.check(checkedId)

            RxRadioGroup.checkedChanges(settingsImageSourceRadioGroup)
                .skipInitialValue()
                .subscribe { Settings.localCrawl = (it == settingsLocalCrawl.id) }
                .also { disposables.add(it) }
        }
    }

    private fun configureTransformsLocation() {
        val supportedList = listOf(CrawlerType.PROJECT_REACTOR, CrawlerType.RX_OBSERVABLE)
        val remoteSupported = supportedList.contains(Settings.crawlStrategy)

        with(binding) {
            settingsTransformsSourceRadioGroup.enable(remoteSupported)
            settingsTransformsLabel.enable(remoteSupported)
            val checkedId =
                if (Settings.localTransforms || !remoteSupported) {
                    settingsLocalTransforms.id
                } else {
                    settingsRemoteTransforms.id
                }
            settingsTransformsSourceRadioGroup.check(checkedId)

            RxRadioGroup.checkedChanges(settingsTransformsSourceRadioGroup)
                .skipInitialValue()
                .subscribe { Settings.localTransforms = (it == settingsLocalTransforms.id) }
                .also { disposables.add(it) }
        }
    }

    private fun configureDebugLogging() {
        with(binding) {
            settingsDebugOutput.isChecked = Settings.debugLogging
            RxCompoundButton.checkedChanges(settingsDebugOutput)
                .skipInitialValue()
                .subscribe { Settings.debugLogging = it }
                .also { disposables.add(it) }
        }
    }

    private fun configureThreadSpeed() {
        with(binding) {
            speedSeekBar.grabTouchEvents()
            speedSeekBar.progress = Settings.crawlSpeed
            RxSeekBar.userChanges(speedSeekBar)
                // Don't set shared pref if configuring.
                .skipInitialValue()
                .subscribe { Settings.crawlSpeed = it }
                .also { disposables.add(it) }
        }
    }

    private fun configureShowSpeedBar() {
        with(binding) {
            settingsShowSpeedBar.isChecked =
                Settings.speedBarState != STATE_COLLAPSED
            RxCompoundButton.checkedChanges(settingsShowSpeedBar)
                // Don't set shared pref if configuring.
                .skipInitialValue()
                .subscribe {
                    val currentState = Settings.speedBarState
                    if (it) {
                        if (currentState == STATE_HIDDEN) {
                            Settings.speedBarState = STATE_EXPANDED
                        }
                    } else {
                        if (currentState != STATE_HIDDEN) {
                            Settings.speedBarState = STATE_HIDDEN
                        }
                    }
                }
                .also { disposables.add(it) }
        }
    }

    private fun configureViewScale() {
        with(binding) {
            viewScaleSeekBar.grabTouchEvents()
            viewScaleSeekBar.max = GRID_SCALE_RANGE.progressMax
            viewScaleSeekBar.progress =
                GRID_SCALE_RANGE.toProgress(Settings.viewScale)
            RxSeekBar.userChanges(viewScaleSeekBar)
                .skipInitialValue()
                .subscribe {
                    val value = GRID_SCALE_RANGE.fromProgress(it)
                    Settings.viewScale = value
                }
                .also { disposables.add(it) }
        }
    }

    private fun configureViewTransparency() {
        with(binding) {
            viewTransparencySeekBar.grabTouchEvents()
            viewTransparencySeekBar.max = TRANSPARENCY_RANGE.progressMax
            viewTransparencySeekBar.progress =
                TRANSPARENCY_RANGE.toProgress(Settings.viewTransparency)
            RxSeekBar.userChanges(viewTransparencySeekBar)
                .subscribe {
                    val value = TRANSPARENCY_RANGE.fromProgress(it)
                    Settings.viewTransparency = value
                    view?.alpha = 1 - (value * (1 / 100f))
                }
                .also { disposables.add(it) }
        }
    }

    private fun configureShowProgress() {
        with(binding) {
            settingsShowProgress.isChecked = Settings.showProgress
            RxCompoundButton.checkedChanges(settingsShowProgress)
                .skipInitialValue()
                .subscribe { Settings.showProgress = it }
                .also { disposables.add(it) }
        }
    }

    private fun configureShowState() {
        with(binding) {
            settingsShowState.isChecked = Settings.showState
            RxCompoundButton.checkedChanges(settingsShowState)
                .skipInitialValue()
                .subscribe { Settings.showState = it }
                .also { disposables.add(it) }
        }
    }

    private fun configureShowThread() {
        with(binding) {
            settingsShowThread.isChecked = Settings.showThread
            RxCompoundButton.checkedChanges(settingsShowThread)
                .skipInitialValue()
                .subscribe { Settings.showThread = it }
                .also { disposables.add(it) }
        }
    }

    private fun configureShowSize() {
        with(binding) {
            settingsShowSize.isChecked = Settings.showSize
            RxCompoundButton.checkedChanges(settingsShowSize)
                .skipInitialValue()
                .subscribe { Settings.showSize = it }
                .also { disposables.add(it) }
        }
    }

    private fun configureSaveOnExit() {
        with(binding) {
            settingsSaveOnExit.isChecked = Settings.saveOnExit
            compositeDisposable.add(
                RxCompoundButton.checkedChanges(settingsSaveOnExit)
                    .skipInitialValue()
                    .subscribe {
                        updatePreference { Settings.saveOnExit = it }
                    })
        }
    }

    /**
     * NOTE: This method is still used but the userAction property
     * is no longer used in onSharedPreferenceChanged because of the
     * edge case issues described by the WARNING note.
     *
     * All Observables call this method to do the preference update
     * so that the userAction flag can be set first which prevents
     * this class's shared preference listener from reacting to the
     * preference change event. The flag is reset to false after
     * the update so that the listener is no longer disabled.
     *
     * WARNING: There's and edge case where the app may update a
     * shared preference while this function has set userAction
     * to true, and therefore this class's onSharedPreference
     * handler will inadvertently process the notification in the
     * userAction block - this can cause unexpected results and
     * should be fixed.
     */
    private fun updatePreference(block: () -> Unit) {
        userAction = true
        block()
        userAction = false
    }

    /**
     * Keeps settings panel up to date if any shared preferences are changed
     * else where in the app or when the reset button is clicked. Note that
     * for efficiency reasons, we don't want to reconfigure an preference
     * widget if the preference changed was itself caused by user input in
     * this Settings panel.
     *
     * NOTE: This method no longer uses the userAction flag set by
     * updatePreference function to prevent the edge case issue
     * described in the following WARNING note.
     *
     * WARNING: There's and edge case where the app may internally update a
     * shared preference while userAction is true which will cause this
     * function to inadvertently process the internal notification in the
     * userAction block - this can cause unexpected results and should be
     * fixed. The reason for this is when the updatePreference sets the
     * shared preference, the onSharedPreference handler of another class
     * may be called and from that call, may set a shared preference before
     * returning from it's onSharedPreference handler. The framework will
     * then call this function for the apps shared preference change before
     * it calls this function for resulting from the local updatePreference
     * call.
     */
    override fun onSharedPreferenceChanged(
        sharedPreferences: SharedPreferences?,
        key: String?
    ) {
        // React to preference changes made outside of this view.
        with(Settings) {
            when (key) {
                CRAWL_STRATEGY_PREF -> {
                    configureCrawlerStrategy()
                    configureTransformsLocation()
                }
                LOCAL_CRAWL_PREF -> configureCrawlerLocation()
                CRAWL_DEPTH_PREF -> configureCrawlerMaxDepth()
                TRANSFORM_TYPES_PREF -> configureTransforms()
                CRAWL_SPEED_PREF -> configureThreadSpeed()
                DEBUG_LOGGING_PREF -> configureDebugLogging()
                SPEED_BAR_STATE_PREF -> configureShowSpeedBar()
                GRID_SCALE_PREF -> configureViewScale()
                TRANSPARENCY_PREF -> configureViewTransparency()
                SHOW_PROGRESS_PREF -> configureShowProgress()
                SHOW_STATE_PREF -> configureShowState()
                SHOW_SIZE_PREF -> configureShowSize()
                SHOW_THREAD_PREF -> configureShowThread()
                LOCAL_TRANSFORMS_PREF -> configureTransformsLocation()
                SAVE_ON_EXIT_PREF -> configureSaveOnExit()
            }
        }
    }

    /**
     * Called to disable model parameter settings when
     * the simulation is running and to re-enable them
     * once the simulation completes.
     */
    fun crawlerRunning(running: Boolean) {
        crawlerRunning = running
        with(binding) {
            settingsCrawlStrategy.enable(!crawlerRunning)
            settingsCrawlDepth.enable(!crawlerRunning)
            settingsLocalCrawl.enable(!crawlerRunning)
            settingsTransformsSourceRadioGroup.enable(!crawlerRunning)
            settingsLocalTransforms.enable(!crawlerRunning)
        }
    }
}
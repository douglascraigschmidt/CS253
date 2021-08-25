package edu.vanderbilt.crawler.ui.screens.settings

import android.util.Range
import com.google.android.material.bottomsheet.BottomSheetBehavior.STATE_COLLAPSED
import edu.vanderbilt.crawler.preferences.Adapter
import edu.vanderbilt.crawler.preferences.Preference
import edu.vanderbilt.crawler.preferences.PreferenceProvider.prefs
import edu.vanderbilt.imagecrawler.crawlers.CrawlerType
import edu.vanderbilt.imagecrawler.transforms.Transform
import edu.vanderbilt.imagecrawler.utils.Options

/**
 * All settings that are saved/restored from shared
 * preferences and changeable from the SettingsDialogFragment.
 */
internal object Settings {
    /** Default pref values */
    private val DEFAULT_CRAWL_STRATEGY = CrawlerType.SEQUENTIAL_LOOPS
    private const val DEFAULT_LOCAL_CRAWL = false
    const val DEFAULT_CRAWL_DEPTH = 3
    private val DEFAULT_TRANSFORM_TYPES = Transform.Type.values().toList()
    const val DEFAULT_CRAWL_SPEED = 100 // [0..100]%
    private const val DEFAULT_DEBUG_LOGGING = false
    private const val DEFAULT_SPEED_BAR_STATE = STATE_COLLAPSED
    private val DEFAULT_WEB_URL = Options.DEFAULT_WEB_URL
    private const val DEFAULT_GRID_SCALE = 6
    private const val DEFAULT_TRANSPARENCY = 10
    private const val DEFAULT_SHOW_PROGRESS = false
    private const val DEFAULT_SHOW_STATE = false
    private const val DEFAULT_SHOW_SIZE = false
    private const val DEFAULT_SHOW_THREAD = false
    private const val DEFAULT_LOCAL_TRANSFORMS = false
    private const val DEFAULT_SAVE_ON_EXIT = true

    /** Pref keys */
    const val CRAWL_STRATEGY_PREF = "crawlStrategyPref"
    const val LOCAL_CRAWL_PREF = "localCrawlPref"
    const val CRAWL_DEPTH_PREF = "crawlDepthPref"
    const val TRANSFORM_TYPES_PREF = "transformTypesPref"
    const val CRAWL_SPEED_PREF = "crawlSpeedPref"
    const val DEBUG_LOGGING_PREF = "debugLoggingPref"
    const val SPEED_BAR_STATE_PREF = "speedBarStatePref"
    const val WEB_URL_PREF = "webUrlPref"
    const val GRID_SCALE_PREF = "gridViewScalePref"
    const val TRANSPARENCY_PREF = "transparencyPref"
    const val SHOW_PROGRESS_PREF = "showProgressPref"
    const val SHOW_STATE_PREF = "showStatePref"
    const val SHOW_SIZE_PREF = "showSizePref"
    const val SHOW_THREAD_PREF = "showThreadPref"
    const val LOCAL_TRANSFORMS_PREF = "localTransformsPref"
    const val SAVE_ON_EXIT_PREF = "SaveOnExitPref"

    /** SeekBar min/max range. */
    val TRANSPARENCY_RANGE = Range(5, 50)
    val GRID_SCALE_RANGE = Range(2, 10)

    /** Pref values */
    var crawlStrategy: CrawlerType by Preference(DEFAULT_CRAWL_STRATEGY, CRAWL_STRATEGY_PREF)
    var localCrawl: Boolean by Preference(DEFAULT_LOCAL_CRAWL, LOCAL_CRAWL_PREF)
    var crawlDepth: Int by Preference(DEFAULT_CRAWL_DEPTH, CRAWL_DEPTH_PREF)
    var transformTypes: List<Transform.Type?> by Preference(DEFAULT_TRANSFORM_TYPES, TRANSFORM_TYPES_PREF)
    var crawlSpeed: Int by Preference(DEFAULT_CRAWL_SPEED, CRAWL_SPEED_PREF)
    var debugLogging: Boolean by Preference(DEFAULT_DEBUG_LOGGING, DEBUG_LOGGING_PREF)
    var speedBarState: Int by Preference(DEFAULT_SPEED_BAR_STATE, SPEED_BAR_STATE_PREF)
    var webUrl: String by Preference(DEFAULT_WEB_URL, WEB_URL_PREF)
    var viewTransparency: Int by Preference(DEFAULT_TRANSPARENCY, TRANSPARENCY_PREF)
    var viewScale: Int by Preference(DEFAULT_GRID_SCALE, GRID_SCALE_PREF)
    var showProgress: Boolean by Preference(DEFAULT_SHOW_PROGRESS, SHOW_PROGRESS_PREF)
    var showState: Boolean by Preference(DEFAULT_SHOW_STATE, SHOW_STATE_PREF)
    var showSize: Boolean by Preference(DEFAULT_SHOW_SIZE, SHOW_SIZE_PREF)
    var showThread: Boolean by Preference(DEFAULT_SHOW_THREAD, SHOW_THREAD_PREF)
    var localTransforms: Boolean by Preference(DEFAULT_LOCAL_TRANSFORMS, LOCAL_TRANSFORMS_PREF)
    var saveOnExit: Boolean by Preference(DEFAULT_SAVE_ON_EXIT, SAVE_ON_EXIT_PREF)

    fun reset() {
        with(prefs.edit()) {
            clear()
            apply()
        }
    }

    fun resetToDefaults(simulationRunning: Boolean) {
        if (!simulationRunning) {
            crawlStrategy = DEFAULT_CRAWL_STRATEGY
            crawlDepth = DEFAULT_CRAWL_DEPTH
            localCrawl = DEFAULT_LOCAL_CRAWL
            transformTypes = DEFAULT_TRANSFORM_TYPES
            webUrl= DEFAULT_WEB_URL
        }

        crawlSpeed = DEFAULT_CRAWL_SPEED
        debugLogging = DEFAULT_DEBUG_LOGGING
        speedBarState = DEFAULT_SPEED_BAR_STATE
        viewScale= DEFAULT_GRID_SCALE
        viewTransparency = DEFAULT_TRANSPARENCY
        showProgress = DEFAULT_SHOW_PROGRESS
        showState = DEFAULT_SHOW_STATE
        showSize = DEFAULT_SHOW_SIZE
        showThread = DEFAULT_SHOW_THREAD
        localTransforms = DEFAULT_LOCAL_TRANSFORMS
        saveOnExit = DEFAULT_SAVE_ON_EXIT
    }
}

/**
 * All Range extensions shift the upper value to be relative to 0.
 */
val Range<Int>.progressMax get() = upper - lower
val Range<Int>.max get() = upper
val Range<Int>.min get() = lower
val Range<Int>.size get() = upper - lower + 1
fun Range<Int>.toProgress(value: Int) = value - lower
fun Range<Int>.fromProgress(value: Int) = lower + value
fun Range<Int>.scale(t: Int): Range<Int> {
    return Range(t * lower, t * upper)
}

fun Range<Int>.encode(): String = "$lower,$upper"

/**
 * Preference adapter class for Range<Int> required since
 * PreferenceObserver can't automatically handle complex objects.
 */
class RangeAdapter : Adapter<Range<Int>> {
    override fun encode(value: Range<Int>) = value.encode()

    override fun decode(string: String): Range<Int> {
        val split = string.split(",")
        if (split.size != 2) {
            error("Unable to decode shared preference Range<Int> value: $string")
        }
        return Range(split[0].toInt(), split[1].toInt())
    }
}

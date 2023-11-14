package edu.vanderbilt.crawler.app

import android.app.Application
import android.content.Context
import android.util.Log
import com.facebook.stetho.Stetho
import com.squareup.picasso.Picasso
import edu.vanderbilt.crawler.BuildConfig
import edu.vanderbilt.crawler.extensions.DelegatesExt.notNullSingleValue
import edu.vanderbilt.crawler.extensions.ImageDownloader
import edu.vanderbilt.crawler.extensions.imageDownloader
import edu.vanderbilt.crawler.utils.globalLogLevel

/**
 * The application made to be easily accessible as a singleton.
 */
class App : Application() {
    lateinit var picasso: Picasso

    companion object {
        @JvmStatic
        var instance: App by notNullSingleValue()
    }

    override fun onCreate() {
        super.onCreate()
        instance = this
        if (BuildConfig.DEBUG) {
            globalLogLevel = Log.VERBOSE
            Stetho.initialize(Stetho.newInitializerBuilder(this)
                    .enableDumpapp(Stetho.defaultDumperPluginsProvider(this))
                    .enableWebKitInspector(Stetho.defaultInspectorModulesProvider(this))
                    .build())
        }

        // Install custom Picasso instance to make it easy
        // to clear it's cache.
        if (imageDownloader == ImageDownloader.PICASSO) {
            val builder = Picasso.Builder(App.instance)
            picasso = builder.build()
        }
    }
}

/**
 * Custom Picasso singleton so that the rest of the app
 * can use Picasso.with(context) as if using the normal
 * Picasso singleton.
 */
object Picasso {
    @Suppress("UNUSED_PARAMETER")
    fun with(context: Context): Picasso {
        return App.instance.picasso
    }

    fun clearCache() {
        App.instance.picasso.shutdown()
        App.instance.picasso = Picasso.Builder(App.instance).build()
    }
}

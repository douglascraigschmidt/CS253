package edu.vanderbilt.crawler.extensions

import android.content.Context
import android.graphics.Bitmap
import android.graphics.drawable.Drawable
import android.widget.ImageView
import com.bumptech.glide.request.Request
import com.bumptech.glide.request.target.SizeReadyCallback
import com.bumptech.glide.request.target.Target
import com.bumptech.glide.request.transition.Transition
import com.bumptech.glide.util.Util
import com.squareup.picasso.Callback
import com.squareup.picasso.MemoryPolicy
import edu.vanderbilt.crawler.R
import edu.vanderbilt.crawler.app.App
import edu.vanderbilt.crawler.app.Picasso
import java.io.ByteArrayOutputStream
import java.net.URL

/**
 * Collection of image helper extensions.
 */

val IMAGE_VIEW_PLACEHOLDER = R.drawable.placeholder

/**
 * Download managers.
 */
enum class ImageDownloader {
    PICASSO
}

/**
 * Current strategy used for downloading images.
 */
var imageDownloader = ImageDownloader.PICASSO

/**
 * Download manager property.
 */
var ImageView.downloader: ImageDownloader
    get() = imageDownloader
    set(downloader) {
        imageDownloader = downloader
    }

/**
 * Check for common image extensions.
 */
fun hasImageExtension(url: String): Boolean {
    return url.endsWith(".png")
            || url.endsWith(".jpg")
            || url.endsWith(".jpeg")
}

/**
 * Loads the gif resource identified by the resource id [imageId].
 * A gif placeholder [placeholder] can be specified. The download
 * requires the imageDownloader to be set to ImageDownloader.GLIDE.
 */
fun ImageView.asyncLoadGif(imageId: Int,
                           placeholder: Int = IMAGE_VIEW_PLACEHOLDER,
                           block: (status: Boolean) -> Unit = {}) {
    asyncLoadGif(context.getResourceUri(imageId).toString(), placeholder, block)
}

/**
 * Loads the gif resource identified by the resource id [imageId].
 * A gif placeholder [placeholder] can be specified. The download
 * requires the imageDownloader to be set to ImageDownloader.GLIDE.
 */
fun ImageView.asyncLoadGif(url: String,
                           placeholder: Int = IMAGE_VIEW_PLACEHOLDER,
                           block: (status: Boolean) -> Unit = {}) {
    (!url.isBlank()).let {
        when (downloader) {
            ImageDownloader.PICASSO -> {
                clear()
                asyncLoad(url, placeholder, block)
            }
        }
    }
}

/**
 * Loads the image or gif (if [asGif] is true) resource identified by
 * the resource id [imageId]. A default placeholder [IMAGE_VIEW_PLACEHOLDER]
 * is used unless a custom [placeholder] value is specified. The download
 * is performed by the current [imageDownloader].
 */
fun ImageView.asyncLoad(imageId: Int,
                        placeholder: Int = IMAGE_VIEW_PLACEHOLDER,
                        block: (status: Boolean) -> Unit = {}) {
    asyncLoad(url = context.getResourceUri(imageId).toString(),
            placeholder = placeholder,
            block = block)
}

internal fun asyncLoadTest(context: Context, url: String) {
//    GlideApp.with(context).load(url)
}

/**
 * Loads the image or gif (if [asGif] is true) from the specified [url].
 * A default placeholder [IMAGE_VIEW_PLACEHOLDER] is used unless a custom
 * [placeholder] value is specified. The download is performed by the
 * current [imageDownloader].
 */
fun ImageView.asyncLoad(url: String,
                        placeholder: Int = IMAGE_VIEW_PLACEHOLDER,
                        block: (status: Boolean) -> Unit = {}) {
    (!url.isBlank()).let {
        when (downloader) {
            ImageDownloader.PICASSO -> {
                clear()
                val builder = Picasso.with(context).load(url)
                builder.placeholder(placeholder)
                builder.into(this,
                        object : Callback {
                            override fun onSuccess() = block(true)
                            override fun onError(e: Exception?) = block(false)
                        })
            }
        }
    }
}

/**
 * Clears the image from the downloader cache.
 */

fun ImageView.clear() {
    when (downloader) {
        ImageDownloader.PICASSO -> Picasso.with(this.context).cancelRequest(this)
    }
}

/**
 * Asynchronously attempts to fetch an image and then calls [block]
 * in the the calling thread passing in the state of the fetch operation.
 */
fun Context.asyncFetchImage(url: String,
                            width: Int = Target.SIZE_ORIGINAL,
                            height: Int = Target.SIZE_ORIGINAL,
                            block: (isImage: Boolean) -> Unit) {
    when (imageDownloader) {
        ImageDownloader.PICASSO -> {
            Picasso.with(this)
                    .load(url)
                    .fetch(object : Callback {
                        override fun onSuccess() = block(true)
                        override fun onError(e: Exception?) = block(false)
                    })
        }
    }
}

/**
 * Replacement class for deprecated Glide SimpleTarget class.
 * We only need this class to download an image in the background
 * and then call a callback with true or false to indicate if the
 * image was successfully downloaded.
 */
class PreloadTarget(val width: Int,
                    val height: Int,
                    val block: (isImage: Boolean) -> Unit) : Target<Bitmap> {
    var requestVar: Request? = null

    override fun onLoadFailed(errorDrawable: Drawable?) {
        block(false)
    }

    override fun onResourceReady(resource: Bitmap, transition: Transition<in Bitmap>?) {
        block(true)
    }

    override fun onLoadStarted(placeholder: Drawable?) {
    }

    override fun getSize(cb: SizeReadyCallback) {
        if (!Util.isValidDimensions(width, height)) {
            throw IllegalArgumentException(
                    "Width and height must both be > 0 or Target#SIZE_ORIGINAL,"
                            + " but given width: $width and height: $height, either provide"
                            + " dimensions in the constructor or call override()")
        }
        cb.onSizeReady(width, height);
    }

    override fun getRequest(): Request? = requestVar
    override fun setRequest(request: Request?) {
        this.requestVar = request
    }

    override fun onStart() {
    }

    override fun onStop() {
    }

    override fun removeCallback(cb: SizeReadyCallback) {
    }

    override fun onLoadCleared(placeholder: Drawable?) {
    }

    override fun onDestroy() {
    }
}

fun URL.getImageBytes(caching: Boolean = true): ByteArray {
    //try {
    val builder = Picasso.with(App.instance)
            .load(IMAGE_VIEW_PLACEHOLDER)
    /*
        val builder = Picasso.with(App.instance)
                .load(this.toString())
                .placeholder(IMAGE_VIEW_PLACEHOLDER)
                */
    if (!caching) {
        builder.memoryPolicy(MemoryPolicy.NO_CACHE, MemoryPolicy.NO_STORE)
    }
    val bitmap = builder.get()
    val stream = ByteArrayOutputStream()
    bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream)
    return stream.toByteArray()
    //} catch (e: Exception) {
    //    println("Picasso unable to fetch image bytes for url $this: $e")
    //    return null
    //}
}


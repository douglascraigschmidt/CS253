package edu.vanderbilt.crawler.platform

import edu.vanderbilt.crawler.app.App
import edu.vanderbilt.crawler.app.Picasso
import edu.vanderbilt.crawler.extensions.ImageDownloader
import edu.vanderbilt.crawler.extensions.imageDownloader
import edu.vanderbilt.imagecrawler.platform.Cache
import java.io.File

object AndroidCache : Cache(File(App.instance.cacheDir, "crawler-cache").canonicalFile) {
    /**
     * Clears both the image-crawler cache and the GLIDE or PICASSO cache.
     */
    override fun clear() {
        super.clear()
        clearDownloaderCache()
    }

    /**
     * Clears only the GLIDE or PICASSO cache.
     */
    fun clearDownloaderCache() {
        when (imageDownloader) {
            ImageDownloader.PICASSO -> {
                Picasso.clearCache()
            }
        }
    }
}

package edu.vanderbilt.imagecrawler.crawlers;

import java.net.URL;
import java.util.Objects;

import edu.vanderbilt.imagecrawler.utils.Crawler;
import edu.vanderbilt.imagecrawler.utils.Image;

import static edu.vanderbilt.imagecrawler.utils.Crawler.Type.IMAGE;
import static edu.vanderbilt.imagecrawler.utils.Crawler.Type.PAGE;

/**
 * This class uses Java sequential streams features to perform an
 * "image crawl" starting from a root Uri. Images from HTML page
 * reachable from the root Uri are downloaded from a remote web server
 * or the local file system and the results are stored in files that
 * are displayed to the user. All stream operations are performed
 * sequentially in a single thread of control.
 */
public class SequentialStreamsCrawler // Loaded via reflection
        extends ImageCrawler {
    /**
     * Recursively crawls the given page and returns the total number
     * of processed images.
     *
     * @param pageUri The URI that's being crawled at this point
     * @param depth   The current depth of the recursive processing
     * @return The number of images processed at this depth
     */
    @Override
    protected int performCrawl(String pageUri, int depth) {
        // Throw an exception if the the stop crawl flag has been set.
        throwExceptionIfCancelled();

        log(">> Depth: " + depth + " [" + pageUri + "]");

        // Return 0 if we've reached the depth limit of the crawl.
        if (depth > mMaxDepth) {
            log("Exceeded max depth of " + mMaxDepth);
            return 0;
        }

        // Atomically check to see if we've already visited this Uri
        // and add the new uri to the hash set so we don't try to
        // revisit it again unnecessarily.
        if (!mUniqueUris.putIfAbsent(pageUri)) {
            log("Already processed " + pageUri);
            // Return 0 if we've already examined this uri.
            return 0;
        }

        // Recursively crawl all images and hyperlinks on this
        // page returning the total number of processed images.
        return crawlPage(pageUri, depth);
    }

    /**
     * Uses Java streams features to (1) download and process images
     * on this page via processImage(), (2) recursively crawl other
     * hyperlinks accessible from this page via performCrawl(), and
     * (3) return a sum of all the image counts.
     *
     * @param pageUri The page uri to crawl.
     * @param depth   The current depth of the recursive processing
     * @return The number of processed images.
     */
    protected int crawlPage(String pageUri, int depth) {
        log("[" + Thread.currentThread().getName()
                + "] Crawling " + pageUri + " (depth " + depth + ")");

        // TODO -- you fill in here replacing this statement with your solution.

        return  mWebPageCrawler.getPage(pageUri)
                .getPageElements(IMAGE, PAGE)
                .stream()
                .mapToInt(e -> e.getType() == IMAGE ? processImage(e.getURL()) : performCrawl(e.getUrl(), depth + 1))
                .sum();
    }

    /**
     * Process an image by applying any transformations that have not
     * already been applied and cached.
     *
     * @param url An image url
     * @return The count of transformed images.
     */
    protected int processImage(URL url) {
        // Use a Java sequential stream to:
        // 1. Convert the transforms array into stream.
        // 2. Try to create a new cached image item for each
        //    transform skipping any that already cached.
        // 3. Transform and store each non-cached image (via map()).
        // 4. Return the count of transformed images (don't count any
        //    images that fail to download correctly).

        // TODO -- you fill in here replacing this statement with your solution.

        Image image = getOrDownloadImage(url);
        // Apply any transforms to this image that have not already
        // been previously applied and cached.
        if(image == null){
            return 0;
        }
        long xx = mTransforms.stream()
                .map(transform ->
                        // Attempt to create a new cache item for this transform
                        // and only apply the transform if a new cache item was
                        // actually created (i.e., was not already in the cache).
                        createNewCacheItem(image, transform) ? applyTransform(transform, image) : null
                )
                .filter(Objects::nonNull)
                .count();

        return (int)xx;
    }
}
package edu.vanderbilt.imagecrawler.crawlers;

import static java.util.stream.Collectors.toList;

import java.net.URL;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;

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

        // Create and use a Java sequential stream to:
        // 1. Use a factory method to create a one-element stream containing
        //    just the pageUri.
        // 2. Use an intermediate operation to filter out pageUri if
        //    it exceeds the depth or has already been visited.
        // 3. Use an intermediate operation to recursively crawl all
        //    images and hyperlinks on this page and return the total
        //    number of processed images.
        // 4. Use a terminal operation to get the total number of
        //    processed images from the one-element stream.

        // TODO -- you fill in here replacing this statement with your solution.
        return 0;
    }

    /**
     * Uses Java streams features to (1) download and process images
     * on this page via processImage(), (2) recursively crawl other
     * hyperlinks accessible from this page via performCrawl(), and
     * (3) return a sum of all the image counts.
     *
     * @param pageUri The page uri to crawl
     * @param depth   The current depth of the recursive processing
     * @return The number of processed images
     */
    protected int crawlPage(String pageUri, int depth) {
        // Create and use a Java sequential stream to:
        // 1. Get the HTML page associated with the pageUri param.
        // 2. Get a list of all image/page links on this page.
        // 3. Convert the list into a stream.
        // 4. Map each web element into the count of images produced
        //    by either processing and image or by crawling a page.
        // 5. Sum all the counts together.

        log("[" + Thread.currentThread().getName()
            + "] Crawling " + pageUri + " (depth " + depth + ")");

        // TODO -- you fill in here replacing this statement with your solution.
        return 0;
    }

    /**
     * Process an image by applying any transformations that have not
     * already been applied and cached
     *
     * @param url An image url
     * @return The count of transformed images
     */
    protected int processImage(URL url) {
        // Create and use a Java sequential stream to:
        // 1. Get or download the image from the given url.
        // 2. Convert the mTransforms array into stream.
        // 3. Try to create a new cached image item for each
        //    transform, skipping any that already cached.
        // 4. Transform and store each non-cached image.
        // 5. Return the count of transformed images (don't count any
        //    images that fail to download or transform correctly).

        // TODO -- you fill in here replacing this statement with your solution.
        return 0;
    }
}

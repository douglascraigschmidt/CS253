package edu.vanderbilt.imagecrawler.crawlers;

import static edu.vanderbilt.imagecrawler.crawlers.Crawler.Type.IMAGE;
import static edu.vanderbilt.imagecrawler.crawlers.Crawler.Type.PAGE;

import java.net.URL;
import java.util.concurrent.ForkJoinPool;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Stream;

import edu.vanderbilt.imagecrawler.platform.Cache;
import edu.vanderbilt.imagecrawler.utils.BlockingTask;
import edu.vanderbilt.imagecrawler.utils.Image;
import edu.vanderbilt.imagecrawler.web.WebPageElement;

/**
 * This {@link ImageCrawler} implementation strategy uses the Java
 * parallel streams framework to perform an "image crawl" starting
 * from a root Uri.  Images from HTML pages reachable from the root
 * Uri are downloaded from a remote web server or the local file
 * system in parallel, transformed, and then stored in files that are
 * displayed to the user.  Likewise, image processing is either
 * performed locally on the client device or remotely using reactive
 * microservices deployed on one or more servers via Spring WebFlux.
 *
 * All stream operations are performed in parallel in the Java common
 * fork-join pool using Java parallel streams.
 */
@SuppressWarnings("unchecked")
public class ParallelStreamsCrawler // Loaded via reflection
       extends ImageCrawler {
    /**
     * Recursively crawls the given page and returns the total number
     * of processed images.
     *
     * @param pageUri The URI that's being crawled at this point
     * @param depth The current depth of the recursive processing
     * @return The count of the number of images processed at this depth
     */
    @Override
    protected int performCrawl(String pageUri, int depth) {
        // Throw an exception if the the stop crawl flag has been set.
        throwExceptionIfCancelled();

        log("[" + Thread.currentThread().getName()
            + "] Crawling " + pageUri + " (depth " + depth + ")");

        // TODO -- you fill in here replacing 'return 0' with your
        // solution, which should create a Java sequential stream that
        // calls crawlPage() and counts the number of images that were
        // processed successfully.  Although this method uses a
        // sequential stream, it should also the correct parallel
        // streams idiom for mapping the stream of streams of process
        // Image objects into a stream of processed Image objects.

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
     * @return A {@link Stream} containing successfully processed images
     */
    protected Stream<Image> crawlPage(String pageUri, int depth) {
        log("[" + Thread.currentThread().getName()
            + "] Crawling " + pageUri + " (depth " + depth + ")");

        // TODO -- you fill in here replacing 'return null' with your
        // solution that uses a Java sequential stream to check if the
        // current depth is within the max depth and if the URI hasn't
        // been visited before and then calls helper methods to get
        // the page and crawl the page.  The helper method that gets
        // the page should be called in the context of another helper
        // method that expands the Java common fork-join pool as
        // needed.  Although this method uses a sequential stream, it
        // should also use the correct parallel streams idiom for (1)
        // first getting the HTML page associated with the uri and (2)
        // then mapping the stream of streams of process Image objects
        // into a stream of processed Image objects.

        return null;
    }

    /**
     * Use a Java sequential stream to (1) download and process images
     * on this page via processImage(), (2) recursively crawl other
     * hyperlinks accessible from this page via performCrawl(), and
     * (3) return the sum of all images processed during the crawl.
     *
     * @param page  The page containing HTML
     * @param depth The current depth of the recursive processing
     * @return A {@link Stream} of successfully processed images
     */
    protected Stream<Image> processPage(Crawler.Page page,
                                        int depth) {
        // TODO -- you fill in here replacing 'return null' with your
        // solution that uses a Java parallel stream and helper
        // methods to get all the IMAGE and PAGE elements from the
        // page and then call helper methods to crawl or process these
        // elements and return a stream of Image objects.  Use the
        // correct parallel streams idiom for reducing the stream of
        // streams of process Image objects into a stream of processed
        // Image objects.

        return null;
    }

    /**
     * Process an image by applying transformations that have not
     * already been applied and cached.
     *
     * @param imageUrl An image url
     * @return A {@link Stream} of successfully transformed images
     */
    protected Stream<Image> processImage(URL imageUrl) {
        // TODO -- you fill in here replacing 'return null' with your
        // solution, which create a one-element Java sequential stream
        // that either gets or downloads an image from the given URL
        // (depending on whether it's cached or not) using helper
        // methods defined in the ImageCrawler super class and calls a
        // helper method in this class to transform each image and
        // return a Stream of transformed images.  Although this
        // method uses a sequential stream, it should also use the
        // correct parallel streams idiom for mapping the stream of
        // streams of process Image objects into a stream of processed
        // Image objects.

        return null;
    }

    /**
     * Locally apply the current set of crawler transforms on the
     * passed {@link Image} and return a {@link Stream} containing all
     * successfully transformed images.
     *
     * @param image The {@link Image} to transform locally
     * @return A {@link Stream} containing non-null transformed images
     */
    protected Stream<Image> transformImage(Image image) {
        // TODO -- you fill in here replacing 'return null' with your
        // solution that uses a Java parallel stream to filter and
        // transform/store an Image using helper methods defined in
        // the ImageCrawler superclass and use the correct parallel
        // streams idiom for returning a Stream containing non-null
        // transformed Image objects. 

        return null;
    }
    /**
     * Use {@link BlockingTask} to encapsulate the {@link Supplier} so
     * it runs in the context of the Java common fork-join pool {@link
     * ForkJoinPool.ManagedBlocker} mechanism.
     *
     * @param supplier The {@link Supplier} to call
     * @return The result of calling the {@link Supplier} in the
     * context of the Java common fork-join pool {@link
     * ForkJoinPool.ManagedBlocker} mechanism
     */
    @Override
    protected <T> T callInManagedBlocker(Supplier<T> supplier) {
        // Use BlockingTask.callInManagedBlock() to run the supplier
        // as a ManagedBlocker.
        // TODO -- you fill in here replacing 'return null' with your
        // solution.

        return null;
    }

    /**
     * Convert {@link Cache.Item} to an Image by downloading it.
     * This call ensures the common fork/join thread pool is expanded
     * to handle the blocking image download.
     *
     * @param item The {@link Cache.Item} to download
     * @return The downloaded {@link Image}
     */
    @Override
    protected Image managedBlockerDownloadImage(Cache.Item item) {
        // Use callInManagedBlocker() and downloadImage() to download
        // the item in a ManagedBlocker.
        // TODO -- you fill in here replacing 'return null' with your
        // solution.

        return null;
    }
}

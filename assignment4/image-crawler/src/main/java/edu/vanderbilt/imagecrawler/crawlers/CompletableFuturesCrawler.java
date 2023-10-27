package edu.vanderbilt.imagecrawler.crawlers;

import static edu.vanderbilt.imagecrawler.crawlers.Crawler.Type.PAGE;

import java.net.URL;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ForkJoinPool;
import java.util.function.Supplier;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import edu.vanderbilt.imagecrawler.platform.Cache;
import edu.vanderbilt.imagecrawler.transforms.Transform;
import edu.vanderbilt.imagecrawler.utils.BlockingTask;
import edu.vanderbilt.imagecrawler.utils.FuturesCollectorIntStream;
import edu.vanderbilt.imagecrawler.utils.Image;

/**
 * This {@link ImageCrawler} implementation strategy uses Java's
 * functional programming features, Java's completable futures
 * framework, Java's sequential streams framework, and the underlying
 * common fork-join pool to perform an "image crawl" asynchronously
 * (and concurrently) starting from a root Uri.  Images from an HTML
 * page reachable from the root Uri are asynchronously downloaded from
 * a remote web server or the local file system, asynchronously
 * transformed, and then stored locally in files on the Android device
 * that can be displayed to the user.
 */
public class CompletableFuturesCrawler
       extends ImageCrawler {
    /**
     * Stores a completed future with value of 0.
     */
    // TODO -- you fill in here replacing null with your solution.
    protected static CompletableFuture<Integer> mZero =
        null;

    /**
     * Perform the web crawl.
     *
     * @param pageUri The URI that's being crawled at this point
     * @param depth   The current depth of the recursive processing
     * @return The number of images downloaded/stored
     */
    protected int performCrawl(String pageUri,
                               int depth) {
        // TODO -- you fill in here replacing 'return 0' with your
        // solution that calls a helper method to perform the crawl
        // asynchronously, block until all asynchronous processing is
        // done, and return the result.  The performCrawl() method is
        // the only one in this class that should call join() (and
        // there should be no calls to get() at all).
        return 0;
    }

    /**
     * Asynchronously perform a web crawl by using Java's completable
     * futures framework to (1) download/transform/store images on
     * this page and (2) crawl other hyperlinks accessible via this
     * page.
     *
     * @param pageUri The URL that we're crawling at this point
     * @param depth   The current depth of the recursive processing
     * @return A {@link CompletableFuture} to the the number of images
     *         downloaded/stored
     */
    protected CompletableFuture<Integer> crawlPageAsync(String pageUri,
                                                        int depth) {
        log(">> Depth: " + depth + " [" + pageUri + "]"
            + " (" + Thread.currentThread().getId() + ")");

        // TODO -- you fill in here replacing 'return null' with your
        // solution that uses a Java sequential stream to check if the
        // current depth is within the max depth and if the URI hasn't
        // been visited before and then uses a chain of intermediate
        // operations that calls helper methods to get the page and
        // crawl the page asynchronously.  At the end of this chain
        // use a terminal operation to get the total number of
        // processed images from the one-element stream.
        return null;
    }

    /**
     * Asynchronously perform the web crawl by using several local
     * CompletableFuture objects to (1) download/transform/store
     * images on this page and (2) crawl other hyperlinks accessible
     * via this page.
     *
     * @param pageFuture A {@link CompletableFuture} to a {@link Crawler.Page}
     *                   that we're crawling at this point
     * @param depth   The current depth of the recursive processing
     * @return A {@link CompletableFuture} to the the number of images
     *         downloaded/stored
     */
    protected CompletableFuture<Integer>
        imagesOnPageAndPageLinksAsync
           (CompletableFuture<Crawler.Page> pageFuture,
                              int depth) {
        // TODO -- you fill in here replacing 'return null' with your
        // solution that calls helper methods that (1) asynchronously
        // download/transform/store all the images in the page
        // associated with the pageFuture after it completes (2)
        // asynchronously/recursively crawl all the links reachable
        // from the pageFuture.  Both these helper methods return
        // CompletableFuture objects to the # of images processed,
        // which are then passed to yet another helper method that
        // combines/sums the results and returns a CompletableFuture.
        return null;
    }

    /**
     * This factory method asynchronously gets the contents of the
     * HTML page at {@code pageUri}.
     *
     * @param pageUri The Uri that we're crawling at this point
     * @return A {@link CompletableFuture} to the HTML page
     */
    protected CompletableFuture<Crawler.Page> getPageAsync(String pageUri) {
        // TODO -- you fill in here replacing 'return null' with your
        // solution that uses a CompletableFuture factory method and a
        // method defined by the mWebPageCrawler field (which should
        // be called via a helper method that expands the Java common
        // fork-join pool) to asynchronously get the HTML page
        // associated with pageUri.
        return null;
    }

    /**
     * Asynchronously download/transform/store all the images in the
     * page associated with {@code pageFuture} after it completes.
     *
     * @param pageFuture A {@link CompletableFuture} to the page being
     *                   downloaded
     * @return A {@link CompletableFuture} to an {@link Integer}
     *         containing the # of images downloaded/stored on this
     *         page
     */
    protected CompletableFuture<Integer>
    imagesOnPageAsync(CompletableFuture<Crawler.Page> pageFuture) {
        // TODO -- you fill in here replacing 'return null' with your
        // solution that calls two helper methods via a fluent chain
        // of CompletableFuture completion stage methods that
        // asynchronously download/transform/store all the images in
        // the page associated with pageFuture after it completes.
        return null;
    }

    /**
     * Asynchronously/recursively crawl all the links reachable from
     * the {@code pageFuture} and returns a CompletableFuture to the #
     * of images downloaded/transformed/stored via this crawl.
     *
     * @param pageFuture A {@link CompletableFuture} to the page
     *                   that's being downloaded
     * @param depth      The current depth of the recursive processing
     * @return A {@link CompletableFuture} to an {@link Integer}
     *         containing the # of images downloaded/transform/stored on
     *         pages linked from this page
     */
    protected CompletableFuture<Integer>
    imagesOnPageLinksAsync(CompletableFuture<Crawler.Page> pageFuture,
                           int depth) {
        // TODO -- you fill in here replacing 'return null' with your
        // solution that uses a single completion stage method that
        // asynchronously calls a helper method that recursively
        // crawls through hyperlinks in 'pageFuture' after it
        // completes.
        return null;
    }

    /**
     * Combines/sums the results of two completable future parameters.
     *
     * @param imagesOnPageFuture A count of the number of images on a
     *                           page
     * @param imagesOnPageLinksFuture A count of the number of images
     *                                on all pages linked from a page
     * @return A {@link CompletableFuture} to the combined/summed
     *         results of the two {@link CompletableFuture} params
     *         after they complete
     */
    protected CompletableFuture<Integer>
        combineResults(CompletableFuture<Integer> imagesOnPageFuture,
                       CompletableFuture<Integer> imagesOnPageLinksFuture) {
        // TODO -- you fill in here replacing 'return null' with your
        // solution that returns a CompletableFuture that
        // synchronously combines/sums the results of the two
        // CompletableFuture params after they complete
        // asynchronously.
        return null;
    }

    /**
     * Recursively crawl through hyperlinks in a {@code page}.
     *
     * @param page  The page containing HTML
     * @param depth The depth of the level of web page traversal
     * @return A {@link CompletableFuture} to an {@link Integer} that
     *         counts how many images were in each hyperlink on the page
     */
    protected CompletableFuture<Integer> crawlHyperLinksOnPage(Crawler.Page page,
                                                               int depth) {
        // TODO -- you fill in here replacing 'return null' with your
        // solution that uses a Java sequential stream containing
        // aggregate operations (e.g., map() and collect()), several
        // CompletableFuture-related methods (e.g., thenApply() and
        // FuturesCollectorIntStream.toFuture()), and other helper
        // methods (e.g., getPageElementsAsStringStream() and
        // crawlPageAsync()) to recursively crawl through hyperlinks
        // in the 'page' parameter.
        return null;
    }

    /**
     * Download, transform, and store images provided via a {@link
     * Stream} of {@code urls}.
     *
     * @param urls A {@link Stream} of URLs corresponding to images on the page
     * @return A {@link CompletableFuture to an {@link Integer} that
     *         counts how many images were downloaded, transformed,
     *         and stored for all {@link URL} objects on the page
     */
    protected CompletableFuture<Integer> processImagesAsync
        (Stream<URL> urls) {
        // TODO -- you fill in here replacing 'return null' with your
        // solution that call intermediate stream operations (e.g.,
        // map(), flatMap(), and collect()), several Completable
        // Future methods (e.g., FuturesCollectorIntStream.toFuture()
        // and thenApply()), and various other methods (e.g.,
        // getOrDownloadImageAsync() in conjunction with the
        // managedBlockerDownloadImage() method reference and
        // transformImageAsync()) to download, transform, and store
        // images via the 'urls' stream.
        return null;
    }

    /**
     * Process the {@code imageFuture} by applying all transforms to
     * it after the {@link CompletableFuture} is triggered when the
     * download completes.
     *
     * @param imageFuture A future to an image that's being downloaded
     * @return A completable future to an stream of Images indicating
     *         the transform operation(s) success or failure
     */
    protected Stream<CompletableFuture<Integer>>
        transformImageAsync(CompletableFuture<Image> imageFuture) {
        // TODO -- you fill in here replacing 'return null' with your
        // solution that applies CompletableFuture methods (e.g.,
        // thenApplyAsync()), stream aggregate operations (e.g.,
        // map()), and a helper method (e.g., transformImage()) to
        // apply all transforms asynchronously after the 'imageFuture'
        // completes.
        return null;
    }

    /**
     * Synchronously apply the {@link Transform} to the {@link Image}
     * and store it in the cache.
     *
     * @param transform The {@link Transform} to apply to the {@link Image}
     * @param image     The {@link Image} to transform and store in the cache.
     * @return 1 if the image was transformed successfully, else 0
     */
    protected int transformImage(Transform transform, Image image) {
        // TODO -- you fill in here replacing 'return 0' with your
        // solution that filter and transform/store an Image using
        // helper methods defined in the ImageCrawler superclass in
        // the context of the mapMulti() intermediate operation,
        // returning 1 if the image was transformed successfully, else
        // 0.
        return 0;
    }

    /**
     * Use {@link BlockingTask} to encapsulate the {@link Supplier} so
     * it runs in the context of the Java common fork-join pool {@link
     * ForkJoinPool.ManagedBlocker} mechanism.
     *
     * @param supplier The {@link Supplier} to call
     * @return The result of calling the {@link Supplier} in the
     *         context of the Java common fork-join pool {@link
     *         ForkJoinPool.ManagedBlocker} mechanism
     */
    @Override
    protected <T> T callInManagedBlocker(Supplier<T> supplier) {
        // TODO -- you fill in here replacing 'return null' with your
        // solution uses BlockingTask.callInManagedBlock() to run the
        // supplier as a ManagedBlocker.
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
        // TODO -- you fill in here replacing 'return null' with your
        // solution that uses callInManagedBlocker() and
        // downloadImage() to download the item in a ManagedBlocker.
        return null;
    }
}

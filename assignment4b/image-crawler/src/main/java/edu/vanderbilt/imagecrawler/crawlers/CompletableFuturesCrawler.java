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
 * framework, and Java's sequential streams framework to perform an
 * "image crawl" asynchronously starting from a root Uri.  Images from
 * an HTML page reachable from the root Uri are downloaded from a
 * remote web server or the local file system, transformed, and then
 * stored locally in files on the Android device that can be displayed
 * to the user.
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
     * @return The number of images downloaded/stored.
     */
    protected int performCrawl(String pageUri,
                               int depth) {
        // Perform the crawl asynchronously, wait until all the
        // processing is done, and return the result.  This is the
        // only method in this class that should call join() (and
        // there should be no calls to get() at all).

        // TODO -- you fill in here replacing return 0 with your
        // solution.
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
     * downloaded/stored
     */
    protected CompletableFuture<Integer> crawlPageAsync(String pageUri,
                                                        int depth) {
        log(">> Depth: " + depth + " [" + pageUri + "]"
            + " (" + Thread.currentThread().getId() + ")");

        // Create and use a Java sequential stream to:
        // 1. Use a factory method to create a one-element stream
        //    containing just the pageUri.
        // 2. Use an intermediate operation to filter out pageUri if
        //    it exceeds max depth or was already visited.
        // 3. Use an intermediate operation to call the method
        //    crawlPageHelper() and return the total number of
        //    processed images.
        // 4. Use a terminal operation to get the total number of
        //    processed images from the one-element stream.

        // TODO -- you fill in here replacing return null with your
        // solution.
        return null;
    }

    /**
     * Asynchronously perform the web crawl by using several local
     * CompletableFuture objects to (1) download/transform/store
     * images on this page and (2) crawl other hyperlinks accessible
     * via this page.
     *
     * @param pageUri The URL that we're crawling at this point
     * @param depth   The current depth of the recursive processing
     * @return A {@link CompletableFuture} to the the number of images
     * downloaded/stored
     */
    protected CompletableFuture<Integer> crawlPageHelper(String pageUri,
                                                         int depth) {
        // Invoke four private method calls as follows:
        // 1. Get a CompletableFuture to the HTML page associated with
        //    pageUri, which is downloaded asynchronously.
        // 2. Get a CompletableFuture to the # of images in this HTML
        //    page, which are downloaded/transformed/stored
        //    asynchronously.
        // 3. Get a CompletableFuture to the # of images linked from
        //    this HTML page, which are downloaded/transformed/stored a
        //    asynchronously/recursively.
        // 4. Return a CompletableFuture that combines/sums the two
        //    CompletableFutures after they complete asynchronously.

        // TODO -- you fill in here replacing return null with your
        // solution.
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
        // Load the HTML page at pageUri asynchronously and return a
        // CompletableFuture to that page.  This method should use a
        // CompletableFuture factory method and a method defined by
        // the mWebPageCrawler field (which should called in the
        // context of the callInManagedBlocker() method that expands
        // the Java common fork-join pool).

        // TODO -- you fill in here replacing return null with your
        // solution.
        return null;
    }

    /**
     * Asynchronously download/transform/store all the images in the
     * page associated with {@code pageFuture} after it is triggered.
     *
     * @param pageFuture A {@link CompletableFuture} to the page being
     *                   downloaded
     * @return A {@link CompletableFuture} to an {@link Integer}
     * containing the # of images downloaded/stored on this
     * page
     */
    protected CompletableFuture<Integer>
        getImagesOnPageAsync(CompletableFuture<Crawler.Page> pageFuture) {
        // Return a CompletableFuture to an Integer containing the #
        // of images processed on this page.  This method should call
        // the getImagesOnPageStream() and processImages() methods via
        // a fluent chain of asynchronous completion stage methods.

        // TODO -- you fill in here replacing return null with your
        // solution.
        return null;
    }

    /**
     * Asynchronously obtain a {@link CompletableFuture} to the # of
     * images processed on pages linked from this page.
     *
     * @param pageFuture A {@link CompletableFuture} to the page
     *                   that's being downloaded
     * @param depth      The current depth of the recursive processing
     * @return A {@link CompletableFuture} to an {@link Integer}
     * containing the # of images downloaded/stored on pages
     * linked from this page
     */
    protected CompletableFuture<Integer>
        crawlHyperLinksOnPageAsync(CompletableFuture<Crawler.Page> pageFuture,
                                   int depth) {
        // Return a CompletableFuture to an Integer containing the #
        // of images processed on pages linked from this page.  This
        // method should use a single completion stage method to call
        // the crawlHyperLinksOnPage() helper method asynchronously.

        // TODO -- you fill in here replacing return null with your
        // solution.
        return null;
    }

    /**
     * Combines/sums the results of two completable future parameters.
     *
     * @param imagesOnPageFuture      A count of the number of images
     *                                on a page
     * @param imagesOnPageLinksFuture A count of the number of images
     *                                on all pages linked from a page
     * @return A {@link CompletableFuture} to the combined/summed
     * results of the two {@link CompletableFuture} params
     * after they complete
     */
    protected CompletableFuture<Integer>
        combineResults(CompletableFuture<Integer> imagesOnPageFuture,
                       CompletableFuture<Integer> imagesOnPageLinksFuture) {
        // Returns a completable future that synchronously
        // combines/sums the results of the two futures params after
        // they complete asynchronously.

        // TODO -- you fill in here replacing return null with your
        // solution.
        return null;
    }

    /**
     * Recursively crawl through hyperlinks in a {@code page}.
     *
     * @param page  The page containing HTML
     * @param depth The depth of the level of web page traversal
     * @return A {@link CompletableFuture} to an {@link Integer} that
     * counts how many images were in each hyperlink on the page
     */
    protected CompletableFuture<Integer> crawlHyperLinksOnPage(Crawler.Page page,
                                                               int depth) {
        // Return a CompletableFuture to an Integer that counts the #
        // of hyperlinks accessible from the page.  This method should
        // consist of a Java sequential stream containing aggregate
        // operations (e.g., map() and collect()), several Completable
        // Future-related methods (e.g., thenApply() and
        // FuturesCollectorIntStream.toFuture()), and other helper
        // methods (e.g., getPageElementsAsStringStream() and
        // crawlPageAsync()).

        // TODO -- you fill in here replacing return null with your
        // solution.
        return null;
    }

    /**
     * Download, transform, and store images provided via a {@link
     * Stream} of {@code urls}.
     *
     * @param urls A {@link Stream} of URLs corresponding to images on the page
     * @return A {@link CompletableFuture to an {@link Integer} that
     * counts how many images were downloaded, transformed,
     * and stored for all {@code urls} on the page
     */
    protected CompletableFuture<Integer> processImages(Stream<URL> urls) {
        // Return a CompletableFuture containing a count of the # of
        // images that were downloaded, transformed, and stored from
        // the List of URLs.  This method should consist of a Java
        // sequential stream that uses aggregate operations (e.g.,
        // map(), collect(), and flatMap()), several completable
        // future methods (e.g., FuturesCollectorIntStream .toFuture()
        // and thenApply()), and various other methods (e.g.,
        // getOrDownloadImageAsync() in conjunction with
        // managedBlockerDownloadImage() and transformImageAsync()).

        // TODO -- you fill in here replacing return null with your
        // solution.
        return null;
    }

    /**
     * Process the {@code imageFuture} by applying all transforms to
     * it after the {@link CompletableFuture} is triggered when the
     * download completes.
     *
     * @param imageFuture A future to an image that's being downloaded
     * @return A completable future to an stream of Images indicating
     * the transform operation(s) success or failure.
     */
    protected Stream<CompletableFuture<Integer>>
        transformImageAsync(CompletableFuture<Image> imageFuture) {
        // Return a Stream of CompletableFuture<Integer> objects to
        // indicate success (1) or failure (0) of the images processed
        // by converting the transforms in the mTransforms field into
        // a sequential Stream.  This method should contain
        // CompletableFuture methods (e.g., thenApplyAsync()), stream
        // aggregate operations (e.g., map()), and a helper method
        // (e.g., transformImage()).

        // TODO -- you fill in here replacing return null with your
        // solution.
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
        // Create and use a Java sequential stream as follows:
        // 1. Use a factory method to create a one-element stream
        //    containing just the transform
        // 2. Use an intermediate operation to try to create a new
        //    cache item for the image, filtering out the image if
        //    it's already been cached locally.
        // 3. Use an intermediate operation to apply the transform to
        //    the original image to produce a new transformed image.
        // 4. Filter out any null transformed images and return the
        //    Stream of filtered images.
        //    Count the # of non-null images transformed.

        // TODO -- you fill in here replacing return 0 with your
        // solution.
        return 0;
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
        // TODO -- you fill in here replacing null with your solution.
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
        // TODO -- you fill in here replacing null with your solution.
        return null;
    }
}

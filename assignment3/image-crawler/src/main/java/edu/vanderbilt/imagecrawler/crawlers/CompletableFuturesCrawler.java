package edu.vanderbilt.imagecrawler.crawlers;

import java.net.URL;
import java.util.concurrent.CompletableFuture;
import java.util.function.Function;
import java.util.stream.Stream;

import edu.vanderbilt.imagecrawler.utils.Array;
import edu.vanderbilt.imagecrawler.utils.Crawler;
import edu.vanderbilt.imagecrawler.utils.FuturesCollectorStream;
import edu.vanderbilt.imagecrawler.utils.Image;

import static edu.vanderbilt.imagecrawler.utils.Crawler.Type.PAGE;

/**
 * This implementation strategy uses Java's functional programming
 * features, Java's completable futures framework, and Java's
 * sequential streams framework to perform an "image crawl"
 * asynchronously starting from a root Uri.  Images from HTML page
 * reachable from the root Uri are downloaded from a remote web server
 * or the local file system and the results are stored in files that
 * can be displayed to the user.
 */
public class CompletableFuturesCrawler
       extends ImageCrawler {
    /**
     * Stores a completed future with value of 0.
     */
    // TODO -- you fill in here replacing this statement with your solution.
    protected CompletableFuture<Integer> mZero =
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
        // only method in this class that should call join().

        // TODO -- you fill in here replacing this statement with your solution.
        return 0;
    }

    /**
     * Asynchronously perform a web crawl by using Java's completable
     * futures framework to (1) download/store images on this page and
     * (2) crawl other hyperlinks accessible via this page.
     *
     * @param pageUri The URL that we're crawling at this point
     * @param depth   The current depth of the recursive processing
     * @return A future to the the number of images downloaded/stored
     */
    protected CompletableFuture<Integer> performCrawlAsync(String pageUri,
                                                           int depth) {
        log(">> Depth: " + depth + " [" + pageUri + "]" + " (" + Thread.currentThread().getId() + ")");

        // Return 0 if we've reached the depth limit of the web
        // crawling.
        if (depth > mMaxDepth) {
            log("Exceeded max depth of " + mMaxDepth);
            return mZero;
        }

        // Atomically check to see if we've already visited the
        // pageUri.  If not, then add pageUri to the cache so we don't
        // try to revisit it again unnecessarily.
        else if (!mUniqueUris.putIfAbsent(pageUri)) {
            log("Already processed " + pageUri);

            // Return 0 if we've already examined this url.
            return mZero;
        } else
            // Invoke the helper method.
            return performCrawlHelper(pageUri,
                                      depth);
    }

    /**
     * Asynchronously perform the web crawl by using several local
     * CompletableFuture objects to (1) download/store images on this
     * page and (2) crawl other hyperlinks accessible via this page.
     *
     * @param pageUri The URL that we're crawling at this point
     * @param depth   The current depth of the recursive processing
     * @return A future to the the number of images downloaded/stored
     */
    protected CompletableFuture<Integer> performCrawlHelper(String pageUri,
                                                            int depth) {
        try {
            // Get a future to the HTML page associated with pageUri,
            // which is downloaded asynchronously.
            CompletableFuture<Crawler.Page> pageFuture =
                getPageAsync(pageUri);

            // The following two asynchronous method calls run
            // concurrently in the common fork-join thread pool after
            // the HTML page download completes.

            // Get a future to the # of images in this HTML page,
            // which are downloaded/stored asynchronously.
            CompletableFuture<Integer> imagesOnPageFuture =
                getImagesOnPageAsync(pageFuture);

            // Get a future to the # of images linked from this page,
            // which are also downloaded/stored asynchronously.
            CompletableFuture<Integer> imagesOnPageLinksFuture =
                crawlHyperLinksOnPageAsync(pageFuture,
                                           // Increment depth.
                                           depth + 1);

            // Return a future to the combined results of the two
            // futures params whenever they complete asynchronously.
            return combineResults(imagesOnPageFuture,
                                  imagesOnPageLinksFuture);
        } catch (Exception e) {
            log("For '" + pageUri + "': " + e.getMessage());
            // Return completed future with value 0 if an exception
            // happens.
            return mZero;
        }
    }

    /**
     * This factory method asynchronously gets the contents of the
     * HTML page at {@code pageUri}.
     *
     * @param pageUri The Uri that we're crawling at this point
     * @return A completable future to the HTML page.
     */
    protected CompletableFuture<Crawler.Page> getPageAsync(String pageUri) {
        // Load the HTML page at pageUri asynchronously and return a
        // completable future to that page.  This method should use a
        // completable future factory method and a method defined on
        // the mWebPageCrawler field.

        // TODO -- you fill in here replacing this statement with your solution.
        return null;
    }

    /**
     * Asynchronously download/store all the images in the page
     * associated with {@code pageFuture} after it is triggered.
     *
     * @param pageFuture A completable future to the page that's being
     *                   downloaded
     * @return A completable future to an integer containing the # of
     *         images downloaded/stored on this page
     */
    protected CompletableFuture<Integer>
        getImagesOnPageAsync(CompletableFuture<Crawler.Page> pageFuture) {
        // Return a completable future to an integer containing the #
        // of images processed on this page.  This method should call
        // the getImagesOnPage() and processImages() methods via a
        // fluent chain of asynchronous completion stage methods.

        // TODO -- you fill in here replacing this statement with your solution.
        return null;
    }

    /**
     * Asynchronously obtain a future to the # of images processed on
     * pages linked from this page.
     *
     * @param pageFuture A completable future to the page that's being
     *                   downloaded
     * @param depth      The current depth of the recursive processing
     * @return A future to an integer containing # of images
     * downloaded/stored on pages linked from this page
     */
    protected CompletableFuture<Integer>
        crawlHyperLinksOnPageAsync(CompletableFuture<Crawler.Page> pageFuture,
                                   int depth) {
        // Return a future to an integer containing the # of images
        // processed on pages linked from this page.  This method
        // should asynchronously call the crawlHyperLinksOnPage()
        // helper method, which recursively crawls all the hyperlinks
        // in the page.

        // TODO -- you fill in here replacing this statement with your solution.
        return null;
    }

    /**
     * Combines the results of two completable future parameters.
     *
     * @param imagesOnPageFuture      A count of the number of images on a page
     * @param imagesOnPageLinksFuture an array of the counts of the
     *                                number of images on all pages
     *                                linked from a page
     * @return A completable future to the combined results of the two
     * futures params after they complete
     */
    protected CompletableFuture<Integer>
        combineResults(CompletableFuture<Integer> imagesOnPageFuture,
                       CompletableFuture<Integer> imagesOnPageLinksFuture) {
        // Returns a completable future that synchronously
        // combines/sums the results of the two futures params after
        // they complete asynchronously.

        // TODO -- you fill in here replacing this statement with your solution.
        return null;
    }

    /**
     * Recursively crawl through hyperlinks in a {@code page}.
     *
     * @param page  The page containing HTML
     * @param depth The depth of the level of web page traversal
     * @return A completable future to an integer that counts how many
     * images were in each hyperlink on the page
     */
    protected CompletableFuture<Integer> crawlHyperLinksOnPage(Crawler.Page page,
                                                               int depth) {
        // Return a completable future to an integer that counts the #
        // of hyperlinks accessible from the page.  This method should
        // consist of one or more Java sequential streams containing
        // aggregate operations (e.g., map(), collect(), and
        // reduce()), several completable future methods (e.g.,
        // FuturesCollectorStream .toFuture() and thenApply()), and
        // various other methods (e.g., getPageElementsAsStrings() and
        // performCrawlAsync()).

        // TODO -- you fill in here replacing this statement with your
        // solution.
        return null;
    }

    /**
     * Download, store, and transform images provided via an array of
     * {@code urls}.
     *
     * @param urls array of URLs corresponding to images on the page
     * @return A completable future to an integer that counts how many
     *         images were downloaded, stored, and transformed for all 
     *         {@code urls} on the page
     */
    protected CompletableFuture<Integer> processImages(Array<URL> urls) {
        // Return a completable future containing a count of the # of
        // images that were downloaded, stored, and transformed from
        // the array of URLs.  This method should consist of one or
        // more Java sequential streams that use aggregate operations
        // (e.g., map(), collect(), flatMap(), and count()), several
        // completable future methods (e.g., FuturesCollectorStream
        // .toFuture() and thenApply()), and various other methods
        // (e.g., downloadAndStoreImageAsync() and
        // transformImageAsync()).

        // TODO -- you fill in here replacing this statement with your
        // solution.
        return null;
    }

    /**
     * Process the {@code imageFuture} by applying all transforms to
     * it after the future is triggered once the download completes.
     *
     * @param imageFuture A future to an image that's being downloaded
     * @return A completable future to an stream of Images indicating
     *         the transform operation(s) success or failure.
     */
    protected CompletableFuture<Stream<Image>>
        transformImageAsync(CompletableFuture<Image> imageFuture) {
        // Return a completable future to an stream of Images that
        // indicate success/failure of the images processed by
        // applying the transforms in the mTransforms field.  This
        // method should contain completable future methods (e.g.,
        // thenCompose() and FuturesCollectorStream.toFuture()),
        // stream aggregate operations (e.g., filter(), map(), and
        // collect()), and other methods (e.g., createNewCacheItem()
        // and applyTransformAsync()).

        // TODO -- you fill in here replacing this statement with your solution.
        return null;
    }
}

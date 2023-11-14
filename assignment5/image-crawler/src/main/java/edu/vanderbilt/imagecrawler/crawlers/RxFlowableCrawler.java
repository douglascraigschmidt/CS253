package edu.vanderbilt.imagecrawler.crawlers;

import static edu.vanderbilt.imagecrawler.crawlers.Crawler.Type.IMAGE;
import static edu.vanderbilt.imagecrawler.crawlers.Crawler.Type.PAGE;

import java.io.IOException;

import edu.vanderbilt.imagecrawler.utils.Image;
import io.reactivex.rxjava3.annotations.NonNull;
import io.reactivex.rxjava3.core.Flowable;
import io.reactivex.rxjava3.exceptions.UndeliverableException;
import io.reactivex.rxjava3.functions.Function;
import io.reactivex.rxjava3.parallel.ParallelFlowable;
import io.reactivex.rxjava3.parallel.ParallelTransformer;
import io.reactivex.rxjava3.plugins.RxJavaPlugins;
import io.reactivex.rxjava3.schedulers.Schedulers;

/**
 * This {@link ImageCrawler} implementation strategy uses the RxJava
 * {@link Flowable} and {@link ParallelFlowable} classes to perform a
 * reactive image crawl starting from a root Uri.  Images from HTML
 * pages reachable from the root Uri are asynchronously downloaded in
 * parallel from a remote web server or from the local file system,
 * transformed, and then stored in files that can be displayed to the
 * user.  This implementation uses the RxJava the {@link Flowable}
 * {@code parallel()} and {@code runOn()} operators to ensure parallel
 * processing of {@link ParallelFlowable} streams.
 */
public class RxFlowableCrawler
    extends ImageCrawler {
    /**
     * Recursively crawls the given page and returns the total number
     * of processed images.
     *
     * @param url   The URL that's being crawled at this point
     * @param depth The current depth of the recursive processing
     * @return The number of images processed at this depth
     */
    @Override
    protected int performCrawl(String url, int depth) {
        // Install error handler to catch any possible
        // UndeliverableException.
        setRxJavaErrorHandler();

        // TODO -- you fill in here replacing 'return 0' with your
        // solution that performs the main crawling operation. This
        // method should call a helper method to initiate the crawl
        // process asynchronously, as well as RxJava operations to
        // handle errors, count images, and block until the Flowable
        // stream completes.
        return 0;
    }

    /**
     * Download and transform all images on this page and then
     * recursively download and transform all images on each of the
     * page links within this page.
     *
     * @param pageUrl A web page url
     * @param depth   The current crawl depth of this page
     * @return A {@link Flowable} that emits transformed {@link Image}
     *         objects
     */
    protected Flowable<Image> crawlPageAsync(String pageUrl, int depth) {
        // TODO -- you fill in here replacing 'return null' with your
        // solution that handles asynchronous crawling of a single web
        // page. This method should employ an RxJava's operator to
        // filter out a page if it exceeds the depth or has already
        // been visited.  It should then use several RxJava operators
        // to call helper methods that map the URL to an HTML page and
        // then process the page to retrieve images asynchronously.
        return null;
    }

    /**
     * Return a {@link Flowable} that emits all images on the page and
     * its linked pages.
     *
     * @param page  The crawler page to crawl
     * @param depth The maximum depth of the crawl
     * @return A {@link Flowable} that emits transformed {@link Image}
     *         objects the {@link Crawler.Page} and its linked pages
     */
    protected Flowable<Image> imagesOnPageAndPageLinksAsync(Crawler.Page page,
                                                            int depth) {
        // TODO -- you fill in here replacing this statement with your
        // solution that processes images from a given page and its
        // linked pages. This method should merge the image streams
        // from the current page and its links, utilizing an RxJava
        // operator that calls two helper methods to process the
        // 'page' asynchronously.
        return null;
    }

    /**
     * Finds all page links on the passed page and crawls each page to
     * download and transform all discovered {@link Image} objects.
     *
     * @param page The page to search for page links
     * @return A {@link Flowable} that emits non-null {@link Image}
     *         transforms
     */
    protected Flowable<Image> imagesOnPageLinksAsync(Crawler.Page page,
                                                     int depth) {
        // TODO -- you fill in here replacing 'return null' with your
        // solution that finds and processes images from all pages
        // linked from 'page'. This method should use RxJava
        // ParallelFlowable operators to recursively explore the links
        // on 'page' and apply image processing to each.
        return null;
    }

    /**
     * Finds, downloads, and transforms all {@link Image} objects on
     * the given page.
     *
     * @param page The {@link Crawler.Page} to search for {@link Image} urls
     * @return A {@link Flowable} stream of {@link Image} transforms
     */
    protected Flowable<Image> imagesOnPageAsync(Crawler.Page page) {
        // TODO -- you fill in here replacing 'return null' with your
        // solution that processes images on a single page. This
        // method should use RxJava ParallelFlowable operators to
        // handle the extraction and transformation of images.  In
        // particular, it should use the RxJava compose() operator in
        // conjunction with mapNotNull() to get or download an image,
        // which is then transformed asynchronously via another helper
        // method called within an RxJava operator.
        return null;
    }

    /**
     * Applies the current set of crawler transforms on the passed
     * image and returns a {@link Flowable} stream of all successfully
     * transformed {@link Image} objects.
     *
     * @param image The {@link Image} to transform
     * @return A {@link Flowable} that emits non-null transformed
     *         {@link Image} objects
     */
    protected Flowable<Image> transformImageAsync(Image image) {
        // TODO -- you fill in here replacing 'return null' with your
        // solution that transforms the given image using all the
        // available transforms. This method should apply these
        // transformations via RxJava ParallelFlowable operators that
        // create a new cache entry and filter out any image transform
        // already in the cache.  It should also use the compose()
        // operator in conjunction with mapNotNull() to apply
        // transforms on each image.
        return null;
    }

    /**
     * Sets a custom error handler to prevent app crashing when from
     * UndeliverableException caused when user fails to start remote
     * microservices.
     */
    protected void setRxJavaErrorHandler() {
        if (RxJavaPlugins.getErrorHandler() != null
            || RxJavaPlugins.isLockdown()) {
            return;
        }

        RxJavaPlugins.setErrorHandler(e -> {
                if (e instanceof UndeliverableException) {
                    e = e.getCause();
                } else if (e instanceof IOException) {
                    // Irrelevant network problem or API that throws
                    // on cancellation.
                    return;
                } else if (e instanceof InterruptedException) {
                    // Some blocking code was interrupted by a dispose
                    // call.
                    return;
                } else if ((e instanceof NullPointerException)
                           || (e instanceof IllegalArgumentException)) {
                    // Likely a bug in the application
                    Thread.currentThread().getUncaughtExceptionHandler()
                        .uncaughtException(Thread.currentThread(), e);
                    return;
                } else if (e instanceof IllegalStateException) {
                    // that's a bug in RxJava or in a custom operator
                    Thread.currentThread().getUncaughtExceptionHandler()
                        .uncaughtException(Thread.currentThread(), e);
                    return;
                }

                System.out.println("Undeliverable exception received, " +
                                   "not sure what to do: " + e);
            });
    }

    /**
     * This {@link ParallelTransformer} can be passed as an argument
     * to a RxJava {@code compose()} call to skip any null values
     * generated in an RxJava stream.
     *
     * @param mapper The function to mapped
     * @param <T>    The upstream input type
     * @param <R>    The downstream output type
     * @return The mapped {@link Flowable} value or an empty {@link
     *         Flowable}
     */
    protected static <T, R> ParallelTransformer<T, R> mapNotNull
        (@NonNull Function<? super T, ? extends R> mapper) {
        return upstream -> upstream
            .flatMap(it -> {
                    R result = mapper.apply(it);
                    if (result == null) {
                        return Flowable.empty();
                    } else {
                        return Flowable.just(result);
                    }
                });
    }
}


package edu.vanderbilt.imagecrawler.crawlers;

import static edu.vanderbilt.imagecrawler.crawlers.Crawler.Type.IMAGE;
import static edu.vanderbilt.imagecrawler.crawlers.Crawler.Type.PAGE;

import java.io.IOException;
import java.util.List;

import edu.vanderbilt.imagecrawler.utils.Image;
import edu.vanderbilt.imagecrawler.web.RemoteDataSource;
import io.reactivex.rxjava3.annotations.NonNull;
import io.reactivex.rxjava3.core.Flowable;
import io.reactivex.rxjava3.exceptions.UndeliverableException;
import io.reactivex.rxjava3.functions.Function;
import io.reactivex.rxjava3.parallel.ParallelFlowable;
import io.reactivex.rxjava3.parallel.ParallelTransformer;
import io.reactivex.rxjava3.plugins.RxJavaPlugins;
import io.reactivex.rxjava3.schedulers.Schedulers;
import okhttp3.MultipartBody;

/**
 * This {@link ImageCrawler} implementation strategy uses the RxJava
 * {@link Flowable} and {@link ParallelFlowable} classes to perform a
 * reactive image crawl starting from a root Uri.  Images from HTML
 * pages reachable from the root Uri are asynchronously downloaded in
 * parallel from a remote web server or from the local file system,
 * transformed, and then stored in files that can be displayed to the
 * user.  This implementation uses the RxJava the {@link Flowable}
 * {@code parallel()} and {@code runOn()} operators to ensure parallel
 * processing of ParallelFlowable streams.
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

        // Perform the following steps.
        // 1. Use a helper method to crawl the page using the
        //    specified uri and depth.
        // 
        // 2. Count the number of elements in the Flowable stream.
        // 
        // 3. Return 0 if any exception is encountered.
        // 
        // 4. Block to get the result.
        // 
        // 5. Convert the result to an int primitive.

        // TODO -- you fill in here replacing 'return 0' with your
        // solution.
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
        // This method should use Flowable operators like
        // fromCallable(), filter(), map(), and flatMap() (this latter
        // operator should call imagesOnPageAndPageLinksAsync()) and
        // should perform the following steps:
        // 
        // 1. Use a factory method to create a Flowable that emits
        //    this pageUri.
        //
        // 2. Filter out page if it exceeds the depth or has already
        //    been visited.
        // 
        // 3. Map the url to a page.
        // 
        // 4. Convert each page to a Flowable stream of images
        //    asynchronously.

        // TODO -- you fill in here replacing 'return null' with your
        // solution.
        return null;
    }

    /**
     * Return a {@link Flowable} that emits all images on the page and
     * its linked pages.
     *
     * @param page  The crawler page to crawl
     * @param depth The maximum depth of the crawl
     * @return A {@link Flowable} that emits transformed {@link Image}
     *         objects the Page and its linked pages
     */
    protected Flowable<Image> imagesOnPageAndPageLinksAsync(Crawler.Page page,
                                                            int depth) {
        // This method should call the imagesOnPageLinksAsync() and
        // imagesOnPageAsync() helper methods and use the Flowable
        // operator mergeWith() to merge all the images into a single
        // Flowable stream.

        // TODO -- you fill in here replacing this statement with your
        // solution.
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
        // This method should call crawlPageAsync() recursively and
        // use Flowable operators like fromIterable(), parallel(),
        // runOn(), flatMap(), and sequential() to perform the
        // processing on the RxJava I/O Scheduler using the following
        // steps:
        // 
        // 1. Use a factory method to convert the List of PAGE objects
        //    into a Flowable stream of PAGE objects.
        // 
        // 2. Convert the Flowable into a ParallelFlowable.
        // 
        // 3. Run each page in parallel using the io scheduler.
        //
        // 4. Flat map each url to a Flowable image stream.
        // 
        // 5. Convert the parallel Flowable into a sequential
        //    Flowable and return this result.

        // TODO -- you fill in here replacing 'return null' with your
        // solution.
        return null;
    }

    /**
     * Finds, downloads, and transforms all {@link Image} objects on
     * the given page.
     *
     * @param page The Page to search for {@link Image} urls
     * @return A {@link Flowable} stream of image transforms
     */
    protected Flowable<Image> imagesOnPageAsync(Crawler.Page page) {
        // This method should call getPageElementsAsUrls(),
        // getOrDownloadImage(), and transformImageAsync() helper
        // methods in conjunction with the Flowable operators
        // fromIterable(), parallel(), runOn(), flatMap(), and
        // sequential() to perform the following steps:
        // 
        // 1. Use a factory method to convert the List of IMAGE
        //    objects into a Flowable stream of IMAGE objects.
        //
        // 2. Convert the Flowable into a ParallelFlowable.
        // 
        // 3. Run each page in parallel using the io scheduler.
        //
        // 4. Use compose() and mapNotNull() to map the image URL to
        //    a (possibly) downloaded image.
        //
        // 5. Perform each image transform in parallel and return the
        //    aggregated ParallelFlowable stream of images.
        //
        // 6. Convert the parallel Flowable into a sequential
        //    Flowable and return this result.

        // TODO -- you fill in here replacing 'return null' with your
        // solution.
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
        // This method should call the createNewCacheItem() and
        // applyTransform() helper methods, as well as use Flowable
        // operators like fromIterable(), filter(), subscribeOn(), and
        // the ParallelTransformer mapNotNull() (mapNotNull()
        // ParallelTransformer is statically defined in this class and
        // must be used in conjunction with the compose() operator) to
        // perform the following steps:
        // 
        // 1. Use a factory method to convert the List of
        //    transforms into a Flowable stream of transforms.
        //
        // 2. Convert the Flowable into a ParallelFlowable.
        //
        // 3. Run each page in parallel using the io scheduler.
        //
        // 4. Try to create a new cache entry and filter out any
        //    image transform that is already in the cache.
        //
        // 5. Apply the transform on the image using compose()
        //    and mapNotNull().
        //
        // 6. Convert the parallel Flowable into a sequential Flowable
        //    and return this result.

        // Check super class flag to determine if transforms
        // should be run locally or on a remote server using
        // microservices.
        if (runRemoteTransforms()) {
            return transformImageRemotely(image);
        } else {
            // TODO -- you fill in here replacing 'return null' with your
            // solution.
            return null;
        }
    }

    /**
     * Calls remote server transform microservices to perform
     * transforms on the passed {@link Image}.
     *
     * @param image The {@link Image} to transform remotely
     * @return A {@link Flowable} that emits transformed {@link
     *         Image} objects
     */
    protected Flowable<Image> transformImageRemotely(Image image) {
        // You don't need to implement this method!
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


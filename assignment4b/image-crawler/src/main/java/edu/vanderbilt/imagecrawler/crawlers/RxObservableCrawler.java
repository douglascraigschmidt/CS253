package edu.vanderbilt.imagecrawler.crawlers;

import java.io.IOException;
import java.net.URL;
import java.util.List;

import edu.vanderbilt.imagecrawler.utils.Image;
import edu.vanderbilt.imagecrawler.web.RemoteDataSource;
import io.reactivex.rxjava3.annotations.NonNull;
import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.core.ObservableTransformer;
import io.reactivex.rxjava3.exceptions.UndeliverableException;
import io.reactivex.rxjava3.functions.Function;
import io.reactivex.rxjava3.plugins.RxJavaPlugins;
import io.reactivex.rxjava3.schedulers.Schedulers;
import okhttp3.MultipartBody;

import static edu.vanderbilt.imagecrawler.crawlers.Crawler.Type.IMAGE;
import static edu.vanderbilt.imagecrawler.crawlers.Crawler.Type.PAGE;

/**
 * This {@link ImageCrawler} implementation strategy uses the RxJava
 * {@link Observable} class to perform a reactive image crawl starting
 * from a root Uri.  Images from HTML pages reachable from the root
 * Uri are asynchronously and concurrently downloaded from a remote
 * web server or from the local file system, transformed, and then
 * stored in files that can be displayed to the user.
 */
public class RxObservableCrawler
       extends ImageCrawler {
    /**
     * Recursively crawl the given page a {@code pageUri} and return
     * the total number of processed images.
     *
     * @param pageUri The URI that's being crawled at this point
     * @param depth   The current depth of the recursive processing
     * @return The number of images processed at this depth
     */
    @Override
    protected int performCrawl(String pageUri, int depth) {
        // Install error handler to catch any possible
        // UndeliverableException.
        setRxJavaErrorHandler();

        // Perform the following steps.
        // 1. Use a helper method to crawl the page using the
        //    specified uri and depth.
        // 
        // 2. Count the number of elements in the Observable stream.
        // 
        // 3. Return 0 if any exception is encountered.
        // 
        // 4. Block to get the result.
        // 
        // 5. Convert the result to an int primitive.

        // TODO -- you fill in here replacing this statement with your
        // solution.
        return 0;
    }

    /**
     * Recursively crawl the web page identified by the passed {@code
     * pageUri} and download/transform/store all discovered images.
     * The recursion ends when the passed depth is exceeded.
     *
     * @param pageUri The uri to crawl
     * @param depth   The maximum depth of the crawl
     * @return An {@link Observable} that emits transformed {@link
     *         Image} objects
     */
    protected Observable<Image> crawlPageAsync(String pageUri,
                                               int depth) {
        // This method should use Observable operators like
        // fromCallable(), filter(), map(), and flatMap() (this latter
        // operator should call processPageAsync()) and should perform
        // the following steps:
        // 
        // 1. Use a factory method to create an Observable that emits
        //    this pageUri.
        //
        // 2. Filter out page if it exceeds the depth or has already
        //    been visited.
        // 
        // 3. Map the url to a page.
        // 
        // 4. Convert each page to an Observable stream of images
        //    asynchronously.

        // TODO -- you fill in here replacing null with your solution.
        return null;
    }

    /**
     * Returns an {@link Observable} that emits all images on the page
     * and all pages hyper-linked from it.
     *
     * @param page The {@link Crawler.Page} to crawl
     * @param depth The maximum depth of the crawl
     * @return An {@link Observable} that emits all {@link Image}
     *         objects on the page and all pages hyper-linked from it
     */
    protected Observable<Image> processPageAsync
        (Crawler.Page page,
         int depth) {
        // This method should call the imagesOnPageLinksAsync() and
        // imagesOnPageAsync() helper methods and use the Observable
        // operator mergeWith() to merge all the images into a single
        // Observable stream.

        // TODO -- you fill in here replacing 'return null' with your
        // solution.
        return null;
    }

    /**
     * Find all page links on the passed page and recursively crawl
     * each page to download, transform, and store all discovered
     * images.
     *
     * @param page The page to search for page links
     * @return An {@link Observable} that emits non-null {@link Image}
     *         transforms
     */
    protected Observable<Image> imagesOnPageLinksAsync
        (Crawler.Page page,
         int depth) {
        // This method should call crawlPageAsync() recursively and
        // use Observable operators like fromIterable(), flatMap(),
        // and subscribeOn() to perform the processing on the RxJava
        // I/O Scheduler using the following steps:
        // 
        // 1. Use a factory method to convert the List of PAGE
        //    objects into an Observable stream of PAGE objects.
        // 
        // 2. Apply the flatMap() concurrency idiom to crawl each page
        //    asynchronously and emit a stream of images that are
        //    downloaded and transformed concurrently via the I/O
        //    scheduler.

        // TODO -- you fill in here replacing 'return null' with your
        // solution.
        return null;
    }

    /**
     * Find, download, and transform all images on the given page.
     *
     * @param page The {@link Crawler.Page} to search for image urls
     * @return A {@link Observable} that emits transformed {@link
     *         Image} objects
     */
    protected Observable<Image> imagesOnPageAsync(Crawler.Page page) {
        // This method should call getPageElementsAsUrls(),
        // getOrDownloadImage(), and transformImage() helper methods
        // in conjunction with the Observable operators fromIterable()
        // and flatMap() to perform the following steps:
        // 
        // 1. Use a factory method to convert the List of IMAGE
        //    objects into an Observable stream of IMAGE objects.
        // 
        // 2. Apply the flatMap() concurrency idiom to download all
        //    the images in parallel.
        // 
        // 3. Again apply the flatMap() concurrency idiom to convert
        //    the stream of downloaded images into a stream of images
        //    are transformed in parallel.

        // TODO -- you fill in here replacing 'return null' with your
        // solution.
        return null;
    }

    /**
     * Asynchronously download and return the {@link Image} at the
     * given {@code url}
     *
     * @param url The {@link URL} to download
     * @return An {@link Observable} that emits a single downloaded
     *         {@link Image}
     */
    protected Observable<Image> downloadImageAsync(URL url) {
        // This method should call the getOrDownloadImage() helper
        // method, as well as use Observable operators like
        // fromCallable(), subscribeOn, compose(), and the
        // ObservableTransformer mapNotNull() (mapNotNull()
        // ObservableTransformer is statically defined in this class
        // and must be used in conjunction with the compose()
        // operator) to perform the following steps:
        // 
        // 1. Use a factory method to create an Observable that emits
        //    the URL.
        // 
        // 2. Run the computation in the I/O scheduler.
        // 
        // 3. Use compose() and mapNotNull() to map the image URL to a
        //    (possibly) downloaded image, using the downloadImage
        //    method reference if the url isn't cached.

        // TODO -- you fill in here replacing 'return null' with your
        // solution.  
        return null;
    }

    /**
     * Apply the current set of crawler transforms on {@link Image}
     * parameter and return an {@link Observable} stream of all
     * successfully transformed images.
     *
     * @param image The {@link Image} to transform
     * @return An {@link Observable} that emits non-null transformed
     *         {@link Image} objects
     */
    protected Observable<Image> transformImageAsync(Image image) {
        // This method should call the createNewCacheItem() and
        // applyTransform() helper methods, as well as use Observable
        // operators like fromIterable(), filter(), subscribeOn(), and
        // the ObservableTransformer mapNotNull() (mapNotNull()
        // ObservableTransformer is statically defined in this class
        // and must be used in conjunction with the compose()
        // operator) to perform the following steps:
        // 
        // 1. Use a factory method to convert the List of
        //    transforms into an Observable stream of transforms.
        // 
        // 2. Run the computations in the I/O scheduler.
        // 
        // 3. Only transform images that haven't already been
        //    transformed.
        // 
        // 4. Apply the transform on the image using compose() and
        //    mapNotNull().

        // Check super class flag to determine if transforms
        // should be run locally or on a remote server using
        // microservices.
        if (runRemoteTransforms()) {
            return transformImageRemotely(image);
        } else {
            // TODO -- you fill in here replacing 'return null' with
            // your solution.
            return null;
        }
    }

    /**
     * Calls remote server transform microservices to perform
     * transforms on the passed {@code image}.
     *
     * @param image Source {@link Image} to transform remotely
     * @return An {@link Observable} that emits transformed
     *         {@link Image} objects
     */
    protected Observable<Image> transformImageRemotely(Image image) {
        // Get remote microservices API instance.
        RemoteDataSource.TransformApi api =
            getRemoteDataSource().getApi();

        // Get the remote data source instance from the super class.
        RemoteDataSource remoteDataSource = getRemoteDataSource();

        // Call RemoteDataSource helper to build a MultipartBody.Part
        // containing the image bytes.

        // TODO -- you fill in here replacing null with your solution.
        

        // Call super class helper method to get the list of
        // transform.  TODO -- you fill in here replacing null with
        // your solution.
        List<String> transformNames = null;

        // Call api method to build a Flux stream of transforms that,
        // once subscribed to, will using microservices to
        // concurrently run all transform operations.

        // TODO -- you fill in here replacing 'return null' with your
        // solution.
        return null;
    }

    /**
     * This {@link ObservableTransformer} can be passed as an argument
     * to a RxJava {@code compose()} call to skip any null values
     * generated in an RxJava stream.
     *
     * @param mapper The {@link Function} to mapped
     * @param <T> The upstream input type
     * @param <R> The downstream output type
     * @return The mapped {@link Observable} value or an empty {@link
     *         Observable}
     */
    protected static <T, R> ObservableTransformer<T, R> mapNotNull
        (@NonNull Function<? super T, ? extends R> mapper) {
        return upstream -> upstream
            .flatMap(it -> {
                    R result = mapper.apply(it);
                    if (result == null) {
                        return Observable.empty();
                    } else {
                        return Observable.just(result);
                    }
                });
    }

    /**
     * Sets a custom error handler to prevent an app from crashing
     * when an {@link UndeliverableException} is received due to a
     * failure to start remote microservice(s).
     */
    protected static void setRxJavaErrorHandler() {
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
                    // Some blocking code was interrupted by a dispose call.
                    return;
                } else if ((e instanceof NullPointerException)
                           || (e instanceof IllegalArgumentException)) {
                    // Likely a bug in the application
                    Thread.currentThread().getUncaughtExceptionHandler()
                        .uncaughtException(Thread.currentThread(), e);
                    return;
                } else if (e instanceof IllegalStateException) {
                    // That's a bug in RxJava or in a custom operator.
                    Thread.currentThread().getUncaughtExceptionHandler()
                        .uncaughtException(Thread.currentThread(), e);
                    return;
                }

                System.out.println("Undeliverable exception received, " +
                                   "not sure what to do: " + e);
            });
    }
}

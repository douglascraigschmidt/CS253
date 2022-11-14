package edu.vanderbilt.imagecrawler.crawlers;

import static java.util.stream.Collectors.toList;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import edu.vanderbilt.imagecrawler.platform.Cache;
import edu.vanderbilt.imagecrawler.platform.Controller;
import edu.vanderbilt.imagecrawler.platform.PlatformImage;
import edu.vanderbilt.imagecrawler.transforms.Transform;
import edu.vanderbilt.imagecrawler.transforms.TransformImageDecorator;
import edu.vanderbilt.imagecrawler.utils.BlockingTask;
import edu.vanderbilt.imagecrawler.utils.ConcurrentHashSet;
import edu.vanderbilt.imagecrawler.utils.Crawler;
import edu.vanderbilt.imagecrawler.utils.ExceptionUtils;
import edu.vanderbilt.imagecrawler.utils.Image;
import edu.vanderbilt.imagecrawler.utils.Options;
import edu.vanderbilt.imagecrawler.utils.WebPageCrawler;
import edu.vanderbilt.imagecrawler.web.RemoteDataSource;
import edu.vanderbilt.imagecrawler.web.TransformedImage;

/**
 * This abstract class factors out methods and fields that are common
 * to all image crawler implementation strategies.
 */
public abstract class ImageCrawler
       implements Runnable {
    /**
     * Flag used to stop/cancel a crawl.
     */
    private static volatile boolean mCancelled;

    /**
     * The List of transforms to applyTransform to the images.
     */
    protected List<Transform> mTransforms;

    /**
     * Flag used to run transforms locally or remotely using
     * microservices.
     */
    protected boolean mLocalTransforms;

    /**
     * A cache of unique URIs that have already been processed.
     */
    protected ConcurrentHashMap.KeySetView<String, Boolean> mUniqueUris;

    /**
     * A web page crawler that parses web pages.
     */
    protected WebPageCrawler mWebPageCrawler;

    /**
     * The maximum crawl depth (from options).
     */
    protected int mMaxDepth;

    /**
     * The cache were images are stored after they have been
     * downloaded and undergone all transformations.
     */
    protected Cache mImageCache;

    /**
     * The root URL or pathname to start the search (from options).
     */
    private String mRootUri;

    /**
     * A Function lambda provided by the controller to create
     * a new platform dependent image object.
     */
    private BiFunction<InputStream, Cache.Item, PlatformImage> mNewImageFunction;

    /**
     * A function lambda that maps a uri to a platform dependant
     * input stream.
     */

    private Function<String, InputStream> mMapUriToInputStream;

    /**
     * Keeps track of how long a given test has run and is also
     * used to check if the crawler is currently running.
     */
    private long mStartTime;

    /**
     * Keeps track of all the execution times.
     */
    private final List<Long> mExecutionTimes = new ArrayList<>();

    /**
     * Controller instance saved for calling log method.
     */
    protected Controller mController;

    /**
     * Used for performing remote transforms using microservices.
     */
    private RemoteDataSource mRemoteDataSource;

    /**
     * Constructor that is only available to inner Factory class to
     * support construction using newInstance().
     */
    protected ImageCrawler() {
    }

    /**
     * @return {@code true} if crawl should be cancelled, {@code false} if not.
     */
    private static boolean isCancelled() {
        return mCancelled;
    }

    /**
     * @return {@code true} if transformations should be performed locally,
     * {@code false} if transformations should be performed using remote
     * microservices.
     */
    private boolean getLocalTransforms() {
        return mController.mOptions.mLocalTransforms;
    }

    /**
     * Throws a CancellationException if the application has
     * decided to cancelled the crawl.
     */
    public static void throwExceptionIfCancelled() {
        if (isCancelled()) {
            Thread.currentThread().interrupt();
            //throw new CancellationException("The crawl has been cancelled.");
        }
    }

    /**
     * Called from Factory after creating a new crawler instance and
     * therefore is declared as "package private". Transfers required
     * values from platform Controller to local fields to show
     * explicitly what this and sub-classes require from the
     * controller instance.
     */
    void initialize(Controller controller) {
        // A function lambda that will map a uri to a platform
        // dependant input stream.
        mMapUriToInputStream = controller::mapUriToInputStream;

        // Store the transformations to applyTransform to each
        // downloaded image.
        mTransforms = controller.mTransforms;

        // Store the root Uri provided by the controller.
        mRootUri = Options.mRootUrl;

        // The maximum depth for this crawl.
        mMaxDepth = controller.mOptions.mMaxDepth;

        // A Function lambda the constructs a new platform
        // dependant image.
        mNewImageFunction = controller::newImage;

        // Setup a new WebPageCrawler passing it the platform
        // dependant url to input stream mapping function (used for
        // access local web pages in app resources or assets).
        mWebPageCrawler = new WebPageCrawler(controller::mapUriToInputStream);

        // Use the cache implementation provided by the application's
        // controller.
        mImageCache = controller.getCache();

        // Initialize the cache of processed Uris.
        mUniqueUris = ConcurrentHashMap.newKeySet();

        // Save controller for calling log method.
        mController = controller;

        // Flag determining if local or remote transforms
        // should be run.
        mLocalTransforms = controller.mOptions.mLocalTransforms;

        // Only create remote data source if remote transforms
        // are enabled.
        if (!mLocalTransforms) {
            mRemoteDataSource =
                new RemoteDataSource(mController.mPlatform.getBaseUrl());
        }
    }

    /**
     * A hook method (also a template method) that does bookkeeping
     * operations and dispatches the subclass's performCrawl() hook
     * method to start implementation strategy processing.
     */
    @Override
    public void run() {
        // Clear the cancelled flag from a previous run.
        mCancelled = false;

        log("Running crawler ...");

        if (mStartTime != 0) {
            throw new IllegalStateException("The crawler is already " +
                                            "running.");
        }

        if (mTransforms == null) {
            throw new IllegalStateException("Initialize() must be called " +
                                            "before run().");
        }

        // Start timing the test run.
        startTiming();

        // Perform the web crawling starting at the root Uri, given an
        // initial depth count of 1.
        long totalImages = performCrawl(mRootUri, 1);

        // Stop timing the test run.
        stopTiming();

        throwExceptionIfCancelled();

        log("Crawl completed normally with %d images added to the cache.",
            totalImages);
    }

    /**
     * Sets a flag that is periodically check at strategic
     * locations to determine if all processing should be
     * cancelled.
     */
    public void stopCrawl() {
        mCancelled = true;
    }

    /**
     * Abstract method required to be implemented by any sub class
     * implementation.
     *
     * @param pageUri The URI that we're crawling at this point
     * @param depth   The current depth of the recursive processing
     * @return A future to the number of images processed
     */
    protected abstract int performCrawl(String pageUri, int depth);

    /**
     * @return A {@link List} of all the IMG SRC URLs in this document.
     */
    @NotNull
    protected List<URL> getImagesOnPageList(Crawler.Page page) {
        log("Getting images on page ...");

        // Return an array of all the IMG SRC URLs in this page.
        return page
            // Select all the image elements in the page.
            .getPageElementsAsUrls(Crawler.Type.IMAGE)

            // Convert the elements to a stream.
            .stream()

            // Remove duplicate image URL strings.
            .distinct()

            // Trigger intermediate operations and add elements to the
            // array.
            .collect(toList());
    }

    /**
     * @return A {@link Stream} of all the IMG SRC URLs in this document.
     */
    @NotNull
    protected Stream<URL> getImagesOnPageStream(Crawler.Page page) {
        log("Getting images on page ...");

        // Return an array of all the IMG SRC URLs in this page.
        return page
            // Select all the image elements in the page.
            .getPageElementsAsUrls(Crawler.Type.IMAGE)

            // Convert the elements to a stream.
            .stream()

            // Remove duplicate image URL strings.
            .distinct();
    }

    /**
     * Factory method that retrieves the image associated with the {@code
     * item} and creates an Image to encapsulate it.
     *
     * @param item The cache item which will receive
     * @return an {@code Image} that encapsulates the {@code item}
     */
    private Image downloadImage(Cache.Item item) {
        // Before downloading the next image, check for cancellation
        // and throw and exception if cancelled.
        throwExceptionIfCancelled();

        // Get the input url that was used to create this cache item.
        String url = item.getSourceUri();

        log("Downloading image ", url);

        // Creates an InputStream from the inputUrl from which to read
        // the image data. The input stream is platform dependant, so
        // we call the controller to provide the platform dependant
        // mapping of the url to an input stream.
        try (InputStream inputStream = mMapUriToInputStream.apply(url)) {

            // Call platform dependant lambda image creating function to
            // create a new platform image from the input stream.
            Image image = new Image(url, mNewImageFunction.apply(inputStream, item));

            // Save the image into the cache.
            try (OutputStream outputStream =
                 item.getOutputStream(Cache.Operation.WRITE, image.size())) {
                image.writeImage(outputStream);
            }

            return image;
        } catch (IOException e) {
            throw ExceptionUtils.unchecked(e);
        }
    }

    /**
     * Calls platform dependant URI mapping method to create
     * an input stream for the passed url.
     *
     * @param url An image URL.
     * @return An open InputStream for accessing URL data.
     */
    public InputStream mapUriToInputStream(String url) {
        return mMapUriToInputStream.apply(url);
    }

    /**
     * Creates a new cached image from the passed {@link InputStream).
     *
     * @param url         The image url.
     * @param tag         The optional image tag (only for transformed images).
     * @param inputStream The image {@link InputStream}.
     * @return A cached {@link Image} instance.
     */
    public Image createImage(String url, @Nullable String tag, InputStream inputStream) {
        Cache.Item item = createNewCacheItem(url, tag);

        // Call platform dependant lambda image creating function to
        // create a new platform image from the input stream.

        try {
            Image image = new Image(url, mNewImageFunction.apply(inputStream, item));
            // Save the image into the cache.
            try (OutputStream outputStream =
                 item.getOutputStream(Cache.Operation.WRITE, image.size())) {
                image.writeImage(outputStream);
            } catch (Exception e) {
                throw ExceptionUtils.unchecked(e);
            }
            return image;
        } catch (Exception e) {
            e.printStackTrace();
            throw ExceptionUtils.unchecked(e);
        }
    }

    /**
     * Creates a cacheable transformed Image instance using the passed
     * base image url/name and the bytes from the {@link
     * TransformedImage} passed back from a call to the remote
     * transform microservices API.
     *
     * @param image            Base image that was transformed
     * @param transformedImage Transformed image returned by API call
     * @return A cacheable transformed {@link Image}
     */
    public Image createImage(Image image, TransformedImage transformedImage) {
        try (InputStream inputStream =
             new ByteArrayInputStream(transformedImage.getBytes())) {
            String url = image.getSourceUrl().toString();
            String tag = transformedImage.getTransformName();
            return createImage(url, tag, inputStream);
        } catch (IOException e) {
            e.printStackTrace();
            throw ExceptionUtils.unchecked(e);
        }
    }

    /**
     * Convert URL to an Image by downloading each image via its URL.
     * This call ensures the common fork/join thread pool is expanded
     * to handle the blocking image download.
     */
    @SuppressWarnings("UnusedReturnValue") // return value is used via Consumer<Image>
    protected Image blockingDownload(Cache.Item item) {
        log("Performing blockingDownload: %s", item.getSourceUri());

        return BlockingTask.callInManagedBlock(() -> downloadImage(item));
    }

    /**
     * Factory method that makes a new {@code TransformDecoratorWithImage}.
     */
    protected TransformImageDecorator
        makeTransformDecoratorWithImage(Transform transform, Image image) {
        return new TransformImageDecorator(transform, image);
    }

    /**
     * Apply the transform to the {@code image}.
     *
     * @param image A downloaded image.
     * @return A a transformed image or null if the transform failed
     */
    @Nullable
    protected Image applyTransform(Transform transform,
                                   Image image) {
        // log("Applying transform to image: %s", image.getSourceUrl());
        log("Applying transform to image: %s", image.getSourceUrl());

        // This method will only be called if a new empty cache entry
        // was created by a call to createNewCacheItem. Get that entry and
        // pass it to the CachingTransformerDecorator.
        Cache.Item item = mImageCache.getItem(
                                              image.getSourceUrl().toString(), transform.getName());

        return makeTransformDecoratorWithImage(transform, image).run(item);
    }

    /**
     * Attempts to add a new cache item for this image transform using
     * the original image and transform name as a lookup key. If
     * the cache doesn't already contain a matching item, addItem will
     * atomically create a cache entry along with a new file based a
     * the key constructed url/transform id pair and will return true
     * (was added). Otherwise, addItem will return false.
     *
     * @return true if the {@code transform} version of image was
     * added to the cache and false if it was not added (already
     * in cache).
     */
    public boolean createNewCacheItem(Image image, Transform transform) {
        log("Attempting to add a cache item for transform: %s",
            image.getSourceUrl());

        // Cache expects a group id string or null which defaults to "raw".
        String tag = transform != null ? transform.getName() : null;

        // Call helper method to do the actual adding.
        return createNewCacheItem(image.getSourceUrl().toString(), tag) != null;
    }

    /**
     * Attempts to add a new cache item for this image using using the
     * specified tag.
     *
     * @return existing or newly created cached item
     */
    public Cache.Item createNewCacheItem(String url, @Nullable String tag) {
        Cache.Item item = mImageCache.getItem(url, tag);
        if (item != null) {
            return item;
        } else {
            return mImageCache.addNewItem(url, tag);
        }
    }

    /**
     * This method first checks the cache for an image that matches the
     * {@code url} and if found returns that; otherwise, it calls the
     * {@code downloadImage} helper method to download the image and then
     * returns the result.
     *
     * @return The image to get from cache or by downloading or null if
     * there was a problem downloading the image (e.g. internet connection).
     */
    @Nullable
    public Image getOrDownloadImage(URL url) {
        log("Getting image: %s", url.toString());

        // Attempt to create and download a new cache item for this image
        // url. The addItem method will either return an existing item
        // if one already exists, OR it will allocate a new cache item
        // and then call the passed lambda Consumer (blockingDownload)
        // passing in the item as a parameter. Blocking download will
        // download the image and store it in the new cache file.
        Cache.Item item =
            mImageCache.addOrGetItem(url.toString(),
                                     null, // No group id required
                                     this::blockingDownload);

        // Now that we have a downloaded cached item, do the following:
        // 1. Get the cached item's input stream.
        // 2. Decorate the the platform dependant image in an Image object.
        // 3. The try block automatically will close the input stream.
        // 4. Return the Image decorator object or null if an exception occurred.
        try (InputStream inputStream = item.getInputStream(Cache.Operation.READ)) {
            return new Image(url, mNewImageFunction.apply(inputStream, item));
        } catch (IOException e) {
            throw ExceptionUtils.unchecked(e);
        }
    }

    /**
     * Asynchronously download an image from the {@code url} parameter
     * and return a CompletableFuture that completes when the image
     * finishes being downloaded and stored in the cache.
     */
    protected CompletableFuture<Image> getOrDownloadImageAsync(URL url) {
        // Asynchronously download/store an Image from the url
        // parameter.
        return CompletableFuture.supplyAsync(() -> getOrDownloadImage(url));
    }

    /**
     * Apply the transform to the {@code image} asynchronously.
     *
     * @param image A downloaded image.
     * @return a future to a transformed image.
     */
    protected CompletableFuture<Image> applyTransformAsync(
                                                           Transform transform,
                                                           Image image
                                                           ) {
        // Asynchronously transform an image.
        return CompletableFuture.supplyAsync(() -> {
                // This method will only be called if a new empty
                // cache entry was created by a check to
                // createNewCacheItem(). Get that entry and pass
                // it to the CachingTransformerDecorator.
                Cache.Item item =
                    getCache().getItem(
                                       image.getSourceUrl().toString(),
                                       transform.getName());


                // Apply a transform on the image.
                return makeTransformDecoratorWithImage(transform, image).run(item);
            });
    }

    /**
     * @return The controller image cache implementation.
     */
    public Cache getCache() {
        return mImageCache;
    }

    /**
     * Return the time needed to execute the test.
     */
    public List<Long> executionTimes() {
        return mExecutionTimes;
    }

    /**
     * Start timing the test run.
     */
    private void startTiming() {
        // Note the start time.
        mStartTime = System.nanoTime();
    }

    /**
     * Stop timing the test run.
     */
    private void stopTiming() {
        mExecutionTimes.add((System.nanoTime() - mStartTime) / 1_000_000);
    }

    /**
     * Conditionally prints the {@code string} depending on the current
     * setting of the Options singleton.
     */
    protected void log(String string, Object... args) {
        mController.log(getClass().getSimpleName() + ": " + string, args);
    }

    /**
     * @return The supported transforms for this crawler strategy.
     */
    public List<Transform.Type> getSupportedTransforms() {
        return Arrays.asList(Transform.Type.values());
    }

    /**
     * Java Utility class used to create new instances of supported
     * crawlers.
     */
    public static class Factory {
        /**
         * Disallow object creation.
         */
        private Factory() {
        }

        /**
         * Creates the specified {@code type} transform. Any images
         * downloaded using this transform will be saved in a folder
         * that has the same name as the transform class.
         *
         * @param crawlerType Type of transform.
         * @param controller  A controller that contains all crawler options and
         *                    platform dependent support methods.
         * @return A transform instance of the specified type.
         */
        public static ImageCrawler newCrawler(CrawlerType crawlerType,
                                              Controller controller) {
            ImageCrawler crawler = crawlerType.newInstance();
            controller.log("Initializing crawler ...");
            crawler.initialize(controller);
            return crawler;
        }

        /**
         * Creates a list of new crawler instances matching the past crawler
         * types.
         *
         * @param crawlerTypes List of crawler types to create.
         * @param controller   A controller that contains all crawler options
         *                     and
         *                     platform dependent support methods.
         * @return A list of new crawler instances.
         */
        public static List<ImageCrawler> newCrawlers(
                                                     List<CrawlerType> crawlerTypes,
                                                     Controller controller
                                                     ) {
            return crawlerTypes
                .stream()
                .map(type -> newCrawler(type, controller))
                .collect(toList());
        }
    }

    /**
     * Flag used to run transforms locally or remotely using
     * microservices.
     */
    protected boolean runLocalTransforms() {
        return mLocalTransforms;
    }

    /**
     * Flag used to run transforms locally or remotely using
     * microservices.
     */
    protected boolean runRemoteTransforms() {
        return !runLocalTransforms();
    }

    /**
     * Accessor for the {@link RemoteDataSource}.
     */
    protected RemoteDataSource getRemoteDataSource() {
        return mRemoteDataSource;
    }

    /**
     * Constructs a list of transform names used by remote
     * microservices API call.
     *
     * @return A list of transform names.
     */
    @NotNull
    protected List<String> getTransformNames() {
        // Build list of transform names for API call.
        return mTransforms
            .stream()
            .map(Transform::getName)
            .collect(toList());
    }
}

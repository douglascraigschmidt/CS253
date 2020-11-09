package edu.vanderbilt.imagecrawler.crawlers;

import java.util.Optional;

import edu.vanderbilt.imagecrawler.utils.Crawler;
import edu.vanderbilt.imagecrawler.utils.Image;
import io.reactivex.Observable;
import io.reactivex.schedulers.Schedulers;

import static edu.vanderbilt.imagecrawler.utils.Crawler.Type.IMAGE;
import static edu.vanderbilt.imagecrawler.utils.Crawler.Type.PAGE;

/**
 * This class uses RxJava Observables to perform a concurrent image
 * crawl starting from a root Uri.  Images from HTML pages reachable
 * from the root Uri are downloaded from a remote web server or from
 * the local file system, transformed, and then stored in files that
 * can be displayed to the user.
 */
public class RxObservableCrawler 
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
        // Call the crawlPageAsync() helper method to perform the
        // crawl asynchronously, obtain a count of the number of
        // images downloaded/processed, wait until all the processing
        // is done, and return the result as an int value.  If any
        // error/exception is encountered return 0.

        // TODO -- you fill in here replacing this statement with your
        // solution.
        return 0;
            // Crawl the page using the specified uri and depth.
            
            // Count the number of elements in the stream of
            // observables.
            
            // Return 0 if any error is encountered.
            
            // Block to get the result.
            
            // Convert the result to an int primitive.
    }

    /**
     * Recursively crawls the web page identified by the passed {@code
     * pageUri} and downloads and transforms all discovered images.
     * The recursion ends when the passed depth is exceeded.
     *
     * @param pageUri The uri to crawl.
     * @param depth   The maximum depth of the crawl.
     * @return An Observable stream of transformed images.
     */
    protected Observable<Image> crawlPageAsync(String pageUri, int depth) {
        // Return an Observable stream of images from this page and
        // all page links recursively reachable from it.  Return an
        // empty Observable if the depth limit of the web crawling is
        // reached or if the pageUri has already been visited.  This
        // method should use Observable methods like just(), filter(),
        // map(), and flatMap() (this latter method should call
        // imagesOnPageAsyncAndPageLinks()).

        // TODO -- you fill in here replacing this statement with your
        // solution.
        return null;
            // Create an observable from this pageUri.
            
            // Filter out page if it exceeds the depth or has already
            // been visited.
            
            // Map the url to page.
            
            // Map each page to an Observable stream of images.
    }

    /**
     * Returns all images on the page and its linked pages.
     *
     * @param page  The crawler page to crawl.
     * @param depth The maximum depth of the crawl.
     * @return all images on the page and its linked pages.
     */
    protected Observable<Image> imagesOnPageAsyncAndPageLinks
        (Crawler.Page page,
         int depth) {
        // Return an Observable stream consisting of all images on
        // this page and any images linked on it.  This method should
        // call imagesOnPageAsyncLinks() and imagesOnPageAsync() and
        // use Observable methods mergeWith() and subscribeOn() to
        // merge all the images into a single Observable stream via
        // the io() Scheduler.

        // TODO -- you fill in here replacing this statement with your
        // solution.
        return null;
    }

    /**
     * Find all page links on the passed page and crawl each page to
     * download and transform all discovered images.
     *
     * @param page The page to search for page links.
     * @return An Observable stream of image transforms.
     */
    protected Observable<Image> imagesOnPageAsyncLinks
        (Crawler.Page page, 
         int depth) {
        // Create and return an Observable stream consisting of images
        // that have been downloaded and transformed.  This method
        // should call crawlPageAsync() recursively and use Observable
        // methods like fromIterable(), flatMap(), and subscribeOn()
        // to perform the processing on the io() Scheduler.

        // TODO -- you fill in here replacing this statement with your
        // solution.
        return null;
            // Convert the list of page links into an Observable
            // stream of page links.

            // Map each page to a stream of downloaded and transformed
            // images.
    }

    /**
     * Finds, downloads, and transforms all images on the given page.
     *
     * @param page The page to search for image urls.
     * @return An Observable stream of image transforms.
     */
    protected Observable<Image> imagesOnPageAsync(Crawler.Page page) {
        // Create and return an Observable stream for this page.  This
        // method implements the RxJava flatMap() concurrency idiom.
        // It should call methods getPageElementsAsUrls(),
        // getOrDownloadImage(), and transformImageAsync(), as well as
        // use Observable methods like just(), flatmap(), map(),
        // filter(), and subscribeOn() to perform processing on the
        // io() scheduler.  The flatMap(), map(), and filter() methods
        // can be called more than once.  In addition, Java Optional
        // methods (such as ofNullable(), isPresent(), and get()) can
        // be used to avoid dealing with null values.

        // TODO -- you fill in here replacing this statement with your
        // solution.
        return null;
            // Create an observable from this page.

            // Map this page to an Observable stream of image URLs.

            // Map the image URL to a possibly downloaded image.

            // Only continue processing if an image was available.

            // Extract the actual image from the Optional wrapper.

            // Map the image to an Observable stream of transformed
            // images.
    }

    /**
     * Applies the current set of crawler transforms on the passed
     * image and returns an Observable stream of all successfully
     * transformed images.
     *
     * @param image The image to transform.
     * @return An Observable stream of transformed images.
     */
    protected Observable<Image> transformImageAsync(Image image) {
        // Return an Observable stream of transformed images.  This
        // method should call the createNewCacheItem() and
        // applyTransform() methods, as well as use Observable methods
        // like fromIterable(), filter(), and map() (filter() and
        // map() can be called more than once).  In addition, Java
        // Optional methods (such as ofNullable(), isPresent(), and
        // get()) can be used to avoid dealing with null values.

        // TODO -- you fill in here replacing this statement with your
        // solution.
        return null;
            // Convert the List of transforms into an Observable
            // stream of transforms.
            
            // Run computations in the I/O scheduler.
            
            // Only transform images that haven't already been
            // transformed.
            
            // Apply the transform on the image.
            
            // Filter out any null returns from applyTransform().
            
            // Extract the actual image from the Optional object.
    }
}

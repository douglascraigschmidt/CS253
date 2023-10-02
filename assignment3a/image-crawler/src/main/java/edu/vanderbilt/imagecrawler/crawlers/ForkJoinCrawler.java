package edu.vanderbilt.imagecrawler.crawlers;

import static edu.vanderbilt.imagecrawler.crawlers.Crawler.Type.IMAGE;
import static edu.vanderbilt.imagecrawler.crawlers.Crawler.Type.PAGE;

import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.ForkJoinTask;
import java.util.concurrent.RecursiveTask;
import java.util.function.Function;
import java.util.function.Supplier;

import edu.vanderbilt.imagecrawler.platform.Cache;
import edu.vanderbilt.imagecrawler.transforms.Transform;
import edu.vanderbilt.imagecrawler.utils.BlockingTask;
import edu.vanderbilt.imagecrawler.utils.ExceptionUtils;
import edu.vanderbilt.imagecrawler.utils.Image;
import edu.vanderbilt.imagecrawler.web.WebPageElement;

/**
 * This {@link ImageCrawler} implementation strategy uses the Java
 * common fork-join framework to perform an "image crawl" starting
 * from a root Uri.  Images from an HTML page reachable from the root
 * Uri are downloaded in parallel from a remote web server or the
 * local file system, transformed, and then stored in files on the
 * Android device, where they can be displayed to the user.  This
 * implementation should not use any Java streams features.
 * <p>
 * This implementation should use no Java streams features and should
 * reuse existing code by make calls to the appropriate methods in the
 * ImageCrawler super class (such as getOrDownloadImage(),
 * applyTransform(), createNewCacheItem(), and downloadImage()) and
 * the WegPageCrawler helper class (such as getPage() and
 * getPageElements()).  Also, make sure to comment your code
 * thoroughly or it will not be reviewed.
 */
public class ForkJoinCrawler
       extends ImageCrawler {
    /**
     * Perform the web crawl using the Java common fork-join pool.
     *
     * @param pageUri The URL that we're crawling at this point
     * @param depth   The current depth of the recursive processing
     * @return The number of images downloaded/transformed/stored
     */
    @Override
    protected int performCrawl(String pageUri, int depth) {
        // TODO -- replace 'return 0' with the appropriate code below
        // that uses the Java common fork-join pool.
        return 0;
    }

    /**
     * This factory method create a new {@link PerformTransformTask}.
     *
     * @param image     The {@link Image} to transform
     * @param transform The {@link Transform} to perform
     * @return A new instance of {@link PerformTransformTask} returned as
     * as {@link ForkJoinTask}
     */
    protected ForkJoinTask<Image> makePerformTransformTask
        (Image image,
         Transform transform) {
        // TODO -- you fill in here replacing 'return null' with your
        // solution.

        return null;
    }

    /**
     * This factory method create a new {@link ProcessImageTask}.
     *
     * @param url The URL to the image to process
     * @return A new {@link ProcessImageTask} that will transform the image
     */
    protected ForkJoinTask<Integer> makeProcessImageTask(String url) {
        // TODO -- you fill in here replacing 'return null' with your
        // solution that makes a new ProcessImageTask.
        return null;
    }

    /**
     * This factory method create a new {@link URLCrawlerTask}.
     *
     * @param pageUri The URL to the page to crawl
     * @param depth   The maximum crawl depth
     * @return A new {@link URLCrawlerTask} instance
     */
    protected ForkJoinTask<Integer> makeURLCrawlerTask
        (String pageUri, int depth) {
        // TODO -- you fill in here replacing 'return null' with your
        // solution that makes a new URLCrawlerTask.

        return null;
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
        // solution to use BlockingTask to encapsulate the Supplier so
        // it runs in the context of the Java common fork-join pool 
        // ForkJoinPool.ManagedBlocker mechanism.
        return null;
    }

    /**
     * Convert {@link Cache.Item} to an Image by downloading it as a
     * ManagedBlocker to ensure the common fork/join thread pool is
     * expanded to handle the blocking image download.
     *
     * @param item The {@link Cache.Item} to download
     * @return The downloaded {@link Image}
     */
    @Override
    protected Image managedBlockerDownloadImage(Cache.Item item) {
        // TODO -- you fill in here replacing 'return null' with your
        // solution convert Cache.Item to an Image by downloading it
        // as a ManagedBlocker to ensure the common fork/join thread
        // pool is expanded to handle the blocking image download.
        return null;
    }

    /**
     * Perform a web crawl from a particular starting point.  By
     * extending {@link RecursiveTask}, instances of this class can be
     * forked/joined in parallel via the Java common fork-join pool.
     */
    public class URLCrawlerTask
        extends RecursiveTask<Integer> {
        // TODO -- add necessary fields.
        

        /**
         * Constructor initializes the fields.
         */
        URLCrawlerTask(String pageUri, int depth) {
            // TODO -- initialize any necessary fields.
            
        }

        /**
         * Perform a web crawl at the URL passed to the constructor.
         *
         * @return The number of images downloaded/transformed/stored
         * starting at the URL passed to the constructor
         */
        @Override
        protected Integer compute() {
            // TODO -- you fill in here replacing 'return 0' with your
            // solution that check if the current depth is within the
            // max depth and if the URI hasn't been visited before and
            // then calls a helper method to crawl the page.
            return 0;
        }

        /**
         * Perform a crawl starting at {@code pageUri} and return the
         * sum of all images processed during the crawl.
         *
         * @param pageUri The URL of the page to crawl
         * @param depth   The current depth of the recursive processing
         * @return The number of images processed during the crawl
         */
        protected int crawlPage(String pageUri, int depth) {
            // TODO -- you fill in here replacing 'return 0' with your
            // solution that gets the HTML page associated with the
            // pageUri and then processes the page.  Use a helper
            // method to expand the Java common fork-join pool as
            // needed.
            return 0;
        }

        /**
         * Use the fork-join framework to (1) download and process
         * images on this page via a ProcessImageTask object, (2)
         * recursively crawl other hyperlinks accessible from this
         * page via a URLCrawlerTask object, and (3) return the count
         * of all images processed during the crawl.
         *
         * @param page  The {@link Crawler.Page} containing the HTML
         * @param depth The current depth of the recursive processing
         * @return The count of the number of images processed
         */
        protected int processPage(Crawler.Page page,
                                  int depth) {
            // TODO -- you fill in here replacing this statement with
            // your solution that uses helper methods to get all the
            // IMAGE and PAGE elements from the page and then uses the
            // Java fork-join framework to crawl/count them
            // accordingly and return a sum of the number of images
            // processed.
            List<ForkJoinTask<Integer>> forks = null;

            // Call a method that joins all the forked tasks and
            // returns a sum of the number of images returned from
            // each task.
            // TODO -- you fill in here replacing 'return 0' with your
            // solution.
            return 0;
        }

        /**
         * Join all the {@link ForkJoinTask} objects and return a sum
         * of the number of images returned from each task.
         *
         * @param forks A {@link List} of {@link ForkJoinTask}
         *              objects containing number of {@link Integer}
         *              objects
         * @return The sum of the number of images processed
         */
        protected int sumResults(List<ForkJoinTask<Integer>> forks) {
            // TODO -- you fill in here replacing 'return 0' with your
            // solution that joins all the ForkJoinTasks and returns a
            // sum of the number of images returned from each task.
            return 0;
        }
    }

    /**
     * Download and process an image.  By extending {@link
     * RecursiveTask}, instances of this class can be forked/joined in
     * parallel by the Java common fork-join pool.
     */
    public class ProcessImageTask
        extends RecursiveTask<Integer> {
        // TODO -- Add any necessary fields here.
        

        /**
         * Constructor initializes the fields.
         *
         * @param imageUri The URL to process
         */
        ProcessImageTask(URL imageUri) {
            // TODO -- initialize any necessary fields.
            
        }

        /**
         * Download and process an image.
         *
         * @return A count of the number of images processed
         */
        @Override
        protected Integer compute() {
            // TODO -- you fill in here replacing 'return null' with
            // your solution that either gets or downloads an image
            // from the given URL (depending on whether it's cached or
            // not) using helper methods defined in the ImageCrawler
            // super class and this class using a ManagedBlocker.
            return null;
        }

        /**
         * Apply the current set of crawler transforms on the {@link
         * Image} param and returns a count of all successfully
         * transformed images.
         *
         * @param image The {@link Image} to transform locally
         * @return The count of the non-null transformed images
         */
        protected int transformImage(Image image) {
            // TODO -- you fill in here replacing this statement with
            //  your solution that applies the current set of crawler
            //  transforms on the Image param and returns a count of
            //  all successfully transformed images.
            List<ForkJoinTask<Image>> forks = null;

            // Call a helper method that joins all the forked tasks
            // and returns the number of non-null images obtained from
            // each task.
            // TODO -- you fill in here replacing 'return 0' with your
            // solution.
            return 0;
        }

        /**
         * Join all the forked tasks and count the number of non-null
         * images returned from each task.
         *
         * @param forks A {@link List} of {@link ForkJoinTask}
         *              objects containing number of {@link Image}
         *              objects
         * @return A count of the number of non-null images transformed
         */
        protected int countTransformations(List<ForkJoinTask<Image>> forks) {
            // TODO -- you fill in here replacing 'return 0' with your
            // solution that joins all the forked tasks and counts the
            // number of non-null images returned from each task.
            return 0;
        }
    }

    /**
     * Perform transform operations.  By extending {@link
     * RecursiveTask}, instances of this class can be forked/joined in
     * parallel by the Java common fork-join pool.
     */
    public class PerformTransformTask
           extends RecursiveTask<Image> {
        // TODO -- add necessary fields.
        

        /**
         * Constructor initializes the fields.
         *
         * @param image     An {@link Image} that's been downloaded
         * @param transform The {@link Transform} to perform
         */
        PerformTransformTask(Image image, Transform transform) {
            // TODO -- initialize any necessary fields.
            
        }

        /**
         * Transform and store an {@link Image}.
         *
         * @return A transformed and stored {@link Image}
         */
        @Override
        protected Image compute() {
            // TODO -- you fill in here replacing 'return null' with
            // your solution that transforms and stores an Image using
            // helper methods defined in the ImageCrawler superclass.
            return null;
        }
    }
}

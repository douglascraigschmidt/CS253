package edu.vanderbilt.imagecrawler.crawlers;

import java.lang.reflect.Constructor;

import edu.vanderbilt.imagecrawler.utils.ExceptionUtils;

/**
 * Supported crawlers that can be applied to download, transform,
 * and store images. The enum values are set to class names so
 * that they can be used to create crawler objects using
 * newInstance() which, in turn, uses reflection to load the class.
 */
public enum CrawlerType {
    SEQUENTIAL_LOOPS("SequentialLoopsCrawler"),
    SEQUENTIAL_STREAMS("SequentialStreamsCrawler"),
    PARALLEL_STREAMS1("ParallelStreamsCrawler1"),
    PARALLEL_STREAMS2("ParallelStreamsCrawler2"),
    COMPLETABLE_FUTURES("CompletableFuturesCrawler"),
    FORK_JOIN("ForkJoinCrawler"),
    KOTLIN_COROUTINES("KotlinCoroutineCrawler"),
    RX_OBSERVABLE("RxObservableCrawler"),
    RX_FLOWABLE("RxFlowableCrawler"),
    PROJECT_REACTOR("ReactorCrawler");

    public final String className;

    CrawlerType(String className) {
        this.className = className;
    }

    public boolean isSupported() {
        return getCrawlerClass() != null;
    }

    private Class<?> getCrawlerClass() {
        try {
            // Create a new JavaClassLoader
            Class<?> clazz = getClass();
            if (clazz == null) {
                return null;
            }

            // Load the target class using its binary name
            return clazz.getClassLoader().loadClass(
                    "edu.vanderbilt.imagecrawler.crawlers." + className);
        } catch (Exception e) {
            return null;
        }
    }

    public ImageCrawler newInstance() {
        try {
            // Load the target class using its binary name
            Class clazz = getCrawlerClass();
            if (clazz == null) {
               throw new IllegalStateException(
                       "newInstance should only be called for a supported ImageCrawler type");
            }

            System.out.println("Loaded class name: " + clazz.getName());

            // Create a new instance from the loaded class
            Constructor constructor = clazz.getConstructor();
            Object crawler = constructor.newInstance();
            return (ImageCrawler) crawler;
        } catch (Exception e) {
            throw ExceptionUtils.unchecked(e);
        }
    }
}

package edu.vanderbilt.imagecrawler.crawlers;

import org.jetbrains.annotations.NotNull;

import java.net.URL;
import java.util.List;
import java.util.stream.Stream;

import edu.vanderbilt.imagecrawler.web.WebPageElement;

/**
 * An interface defining the operations that must be supported by any
 * web crawler implementation.  A {@link Crawler} is expected to
 * initially return a {@link Page} when {@code getPage()} is called.
 *
 * A returned {@code Page} object is required to support provide
 * {@code getPage()} and {@code getImageImage()} methods that return
 * all child {@code PAGE} type objects and all {@code IMAGE} type
 * objects, respectively.
 */
public interface Crawler {
    /**
     * The types of objects that can be returned by a {@link Crawler}.
     */
    enum Type {
        PAGE,
        IMAGE
    }

    /**
     * Returns a {@link Page} object for the specified URL. 
     *
     * @param uri Uri of any page
     * @return The {@link Page} matching the specified {@code uri}
     */
    Page getPage(String uri);

    /**
     * Interface encapsulating all operations that can be performed a
     * {@code PAGE} type object that is returned by the {@link
     * Crawler} {@code getPage()} method.
     */
    interface Page {
        /**
         * Returns all children objects of a given type (PAGE or
         * IMAGE) as a {@link Stream}.
         *
         * @param types Types to retrieve (i.e., {@code PAGE} and/or
         *              {@code IMAGE})
         * @return A {@link Stream} of matching {@link WebPageElement}
         *         objects
         */
        Stream<WebPageElement> getPageElementsAsStream(@NotNull Type... types);

        /**
         * Returns all children objects of a given type (i.e., {@code
         * PAGE} and/or {@code IMAGE}) as a {@link Stream} of {@link
         * String} objects.
         *
         * @param types Types to retrieve (i.e., {@code PAGE} and/or
         *              {@code IMAGE})
         * @return A {@link Stream} of matching {@link String} objects
         */
        Stream<String> getPageElementsAsStringStream(@NotNull Type... types);

        /**
         * Returns all children objects of a given type (i.e., {@code
         * PAGE} and/or {@code IMAGE}).
         *
         * @param types Types to retrieve (i.e., {@code PAGE} and/or
         *         {@code IMAGE}) as a {@link List}
         * @return A {@link List} of matching {@link WebPageElement}
         * objects
         */
        List<WebPageElement> getPageElements(@NotNull Type... types);

        /**
         * Returns all children objects of a given type (i.e., {@code
         * PAGE} and/or {@code IMAGE})
         *
         * @param types Types to retrieve (i.e., {@code
         *              PAGE} and/or {@code IMAGE})
         * @return A {@link List} of matching url {@link String}
         *         objects
         */
        List<String> getPageElementsAsStrings(@NotNull Type... types);

        /**
         * Returns the URLs for all children objects that match the
         * specified type.
         *
         * @param types Types to retrieve (i.e., {@code PAGE} and/or
         *              {@code IMAGE})
         * @return A {@link List} of matching {@link URL} objects
         */
        List<URL> getPageElementsAsUrls(@NotNull Type... types);
    }
}

package edu.vanderbilt.imagecrawler.web;

import java.net.MalformedURLException;
import java.net.URL;

import static edu.vanderbilt.imagecrawler.crawlers.Crawler.Type.IMAGE;
import static edu.vanderbilt.imagecrawler.crawlers.Crawler.Type.PAGE;

import edu.vanderbilt.imagecrawler.crawlers.Crawler;
import edu.vanderbilt.imagecrawler.utils.ExceptionUtils;

/**
 * This class contains methods for returning web page elements
 * from the {@link WebPageCrawler} class.
 */
public class WebPageElement {
    /**
     * A URL that points to either a page or image.
     */
    private final String mUrl;

    /**
     * The type of element pointed to by {@code mUrl}, i.e.,
     * {@code PAGE} or {@code IMAGE}.
     */
    private final Crawler.Type mType;

    /**
     * Constructor initializes the fields.
     */
    public WebPageElement(String url, Crawler.Type type) {
        mUrl = url;
        mType = type;
    }

    /**
     * @return The {@code mUrl} field encapsulated as a {@link URL}
     */
    public URL getURL() {
        return ExceptionUtils
            // Create a URL that converts a checked exception
            // into a runtime exception.
            .rethrowSupplier(() -> new URL(mUrl)).get();
    }

    /**
     * @return The {@code mUrl} field
     */
    public String getUrl() {
        return mUrl;
    }

    /**
     * @return The type of element pointed to by {@code mUrl}, i.e.,
     * {@code PAGE} or {@code IMAGE}
     */
    public Crawler.Type getType() {
        return mType;
    }

    /**
     * This factory method returns a new {@link WebPageElement} object
     * that encapsulates an {@code IMAGE}.
     *
     * @param url The {@code url} to encapsulate.
     * @return A new {@link WebPageElement} object
     *         representing an {@code IMAGE}
     */
    public static WebPageElement newImageElement(String url) {
        return new WebPageElement(url, IMAGE);
    }

    /**
     * This factory method returns a new {@link WebPageElement} object
     * that encapsulates a {@code PAGE}.
     *
     * @param url The {@code url} to encapsulate.
     * @return A new {@link WebPageElement} object
     *         representing a {@code PAGE}
     */
    public static WebPageElement newPageElement(String url) {
        return new WebPageElement(url, PAGE);
    }
}


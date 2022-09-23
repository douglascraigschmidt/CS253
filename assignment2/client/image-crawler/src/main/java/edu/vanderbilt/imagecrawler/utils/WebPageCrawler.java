package edu.vanderbilt.imagecrawler.utils;

import static java.util.stream.Collectors.toList;
import static edu.vanderbilt.imagecrawler.utils.Crawler.Type.PAGE;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import java.io.InputStream;
import java.net.URL;
import java.util.Arrays;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Stream;

import edu.vanderbilt.imagecrawler.crawlers.ImageCrawler;
import edu.vanderbilt.imagecrawler.platform.Controller;

/**
 * This helper class works around deficiencies in the jsoup library
 * (www.jsoup.org), which doesn't make web-based crawling and local
 * filesystem crawling transparent out-of-the-box..
 */
public class WebPageCrawler implements Crawler {
    /**
     * Platform dependent function that mas a uri string to an InputStream.
     */
    private final Function<String, InputStream> mMapUrlToStream;

    /**
     * Constructor required for handling platform dependent local crawling.
     *
     * @param mapUrlToStream A platform dependent function that mas a uri
     *                       string to an InputStream.
     */
    public WebPageCrawler(Function<String, InputStream> mapUrlToStream) {
        mMapUrlToStream = mapUrlToStream;
    }

    /**
     * @return A container that wraps the HTML document associated
     * with the {@code pageUri}.
     */
    public Page getPage(String uri) {
        ImageCrawler.throwExceptionIfCancelled();

        if (mMapUrlToStream != null) {
            // Web page is read from a local source requiring
            // requiring the web page to be read from an input
            // stream.

            if (Controller.loggingEnabled()) {
                System.out.println("***************************************");
                System.out.println("GET CONTAINER URI       = " + uri);
            }

            // Build a base (parent) URI from the passed uri.
            String baseUri = uri;
            if (uri.endsWith(".html") || uri.endsWith("htm")) {
                baseUri = uri.substring(0, uri.lastIndexOf('/')) + "/";
            }

            // Jsoup parser requires the base URI to end with a "/";
            if (!baseUri.endsWith("/")) {
                baseUri += "/";
            }

            // Since this is a local crawl make sure that we don't ever
            // try to get an input stream on just a directory; add the
            // index.html file if necessary to prevent that.
            if (!uri.endsWith("index.html")) {
                uri += "/index.html";
            }

            if (Controller.loggingEnabled()) {
                System.out.println("GET CONTAINER BASE URI  = " + baseUri);
                System.out.println("***************************************");
            }

            // Map the uri to an input stream and call Jsoup to
            // read in stream contents and return a DocumentPage.
            try (InputStream inputStream = mMapUrlToStream.apply(uri)) {
                return new DocumentPage(
                        Jsoup.parse(inputStream, "UTF-8", baseUri),
                        uri);
            } catch (Exception e) {
                System.out.println("!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!");
                System.out.println("getContainer Exception: " + e);
                System.out.println("!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!");
                throw new RuntimeException(e);
            }
        } else {
            // Web page is a remote URL.

            // This function (1) connects to a URL and gets its
            // contents and (2) converts checked exceptions to runtime
            // exceptions.
            Function<String, Document> connect =
                    ExceptionUtils.rethrowFunction(
                            url -> Jsoup.connect(url).get());

            Document apply = connect.apply(uri);
            return new DocumentPage(apply, uri);
        }
    }

    /**
     * Encapsulates/hides the JSoup Document object into a generic container.
     */
    protected static class DocumentPage implements Page {
        private final Document document;

        protected DocumentPage(Document document, String uri) {
            if (Controller.loggingEnabled()) {
                System.out.println(">*********************************************");
                System.out.println("WebPageCrawler: constructor()");
                System.out.println("Constructed document: " + (uri == null ? "NULL" : uri));
                System.out.println("             baseURL: " + (document == null
                        ? "NULL"
                        : document.baseUri()));
                System.out.println("<*********************************************");
            }

            this.document = document;
        }

        /**
         * Returns all children objects of a given type (PAGE or
         * IMAGE) as a {@link Stream}.
         *
         * @param types Types to retrieve (PAGE and/or IMAGE)
         * @return A {@link Stream} of matching WebPageElements
         */
        @Override
        public Stream<WebPageElement> getPageElementsAsStream(Type... types) {
            if (types.length == 0) {
                throw new IllegalArgumentException("At least one type must be specified.");
            }

            ImageCrawler.throwExceptionIfCancelled();

            Stream<Type> typeStream = Arrays
                .stream(types);

            return typeStream
                .flatMap(type -> {
                        if (type == PAGE) {
                            Stream<Element> docStream = document
                                .select("a[href]")
                                .stream();

                            return docStream
                                .map(element -> element.attr("abs:href"))
                                .map(WebPageElement::newPageElement);
                        } else {
                            Stream<Element> imageStream = document
                                .select("img")
                                .stream();

                            return imageStream
                                .map(element -> element.attr("abs:src"))
                                .map(WebPageElement::newImageElement);
                        }
                    });
        }

        /**
         * Returns all children objects of a given type (PAGE or
         * IMAGE) as a {@link List}.
         *
         * @param types Types to retrieve (PAGE and/or IMAGE)
         * @return A {@link List} of matching {@link WebPageElement}
         * objects
         */
        @Override
        public List<WebPageElement> getPageElements(Type... types) {
            ImageCrawler.throwExceptionIfCancelled();
            return getPageElementsAsStream(types)
                .collect(toList());
        }

        /**
         * Returns all children objects of a given type (PAGE or IMAGE)
         * as {@link String} objects.
         *
         * @param types Types to retrieve (PAGE and/or IMAGE)
         * @return A {@link Stream} of matching url {@link String} objects
         */
        @Override
        public Stream<String> getPageElementsAsStringStream(Type... types) {
            return getPageElementsAsStream(types)
                .map(WebPageElement::getUrl);
        }

        /**
         * Returns all children objects of a given type (PAGE or IMAGE).
         *
         * @param types Types to retrieve (PAGE and/or IMAGE)
         * @return A {@link List} of matching url {@link String} objects
         */
        @Override
        public List<String> getPageElementsAsStrings(Type... types) {
            return getPageElementsAsStream(types)
                    .map(WebPageElement::getUrl)
                    .collect(toList());
        }

        /**
         * Returns the URLs for all children objects that match the specified
         * type.
         *
         * @param types Types to retrieve (PAGE and/or IMAGE)
         * @return A {@link List} of matching {@link URL} objects
         */
        @Override
        public List<URL> getPageElementsAsUrls(Type... types) {
            return getPageElementsAsStream(types)
                .map(WebPageElement::getURL)
                .collect(toList());
        }

        private void __printSearchResultsStarting(Type type, String uri, Document doc) {
            if (!Controller.loggingEnabled()) {
                return;
            }

            System.out.println(">*********************************************");
            System.out.println("WebPageCrawler: getObjectAsString()");
            System.out.println("Searching PAGE: " + (uri == null ? "NULL" : uri));
            System.out.println("            baseURL: " + doc.baseUri());
            System.out.println("      Searching For: " + type.name());
        }

        private void __printSearchResults(List<String> results, Document doc) {
            if (!Controller.loggingEnabled()) {
                return;
            }

            System.out.println(
                    "       Result Count: " + (results == null ? "0" : results.size()));
            if (results != null && results.size() > 0) {
                System.out.print("            Results: ");
                for (int i = 0; i < results.size(); i++) {
                    if (i == 0) {
                        System.out.println(results.get(i));
                    } else {
                        System.out.println("                     " + results.get(i));
                    }
                }
            }
            System.out.println("<*********************************************");
        }
    }
}

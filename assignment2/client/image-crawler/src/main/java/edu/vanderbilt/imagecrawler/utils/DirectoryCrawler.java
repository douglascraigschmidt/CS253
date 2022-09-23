package edu.vanderbilt.imagecrawler.utils;

import static java.util.stream.Collectors.toList;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import edu.vanderbilt.imagecrawler.crawlers.ImageCrawler;

import static edu.vanderbilt.imagecrawler.utils.Crawler.Type.PAGE;
import static edu.vanderbilt.imagecrawler.utils.Student.isUndergraduate;
import static edu.vanderbilt.imagecrawler.utils.WebPageElement.newPageElement;

/**
 * Not currently used.
 */
public class DirectoryCrawler implements Crawler {
    @Override
    public Page getPage(String uri) {
        ImageCrawler.throwExceptionIfCancelled();

        try {
            File file = new File(new URI(uri).getPath());
            if (!file.isDirectory()) {
                throw new RuntimeException("Uri is not a directory: " + uri);
            }
            return new DirectoryPage(file, uri);
        } catch (URISyntaxException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Encapsulates/hides the JSoup Document object into a generic container.
     */
    protected class DirectoryPage implements Page {
        private final File mDirectory;

        protected DirectoryPage(File directory, String uri) {
            mDirectory = directory;
        }

        @Override
        public List<WebPageElement> getPageElements(Type... types) {
            return getPageElementsAsStream(types)
                    .collect(toList());
        }

        @Override
        public List<URL> getPageElementsAsUrls(Type... types) {
            return getPageElementsAsStream(types)
                    .map(WebPageElement::getURL)
                    .collect(toList());
        }

        @Override
        public List<String> getPageElementsAsStrings(Type... types) {
            return getPageElementsAsStream(types)
                    .map(WebPageElement::getUrl)
                    .collect(toList());
        }

        @Override
        public Stream<WebPageElement> getPageElementsAsStream(Type... types) {
            if (types.length == 0) {
                throw new IllegalArgumentException("At least one type must be specified.");
            }

            ImageCrawler.throwExceptionIfCancelled();

            File[] files = mDirectory.listFiles();
            if (files == null || files.length == 0) {
                return Stream.empty();
            }

            return Arrays
                    .stream(types)
                    .flatMap(type ->
                            Stream.of(files)
                                    .filter(file ->
                                            type == PAGE
                                                    ? file.isDirectory()
                                                    : isImageFile(file))
                                    .map(File::toURI)
                                    .map(URI::toString)
                                    .map(WebPageElement::newPageElement)
                    );
        }

        @Override
        public Stream<String> getPageElementsAsStringStream(Type... types) {
            return getPageElementsAsStream(types)
                    .map(WebPageElement::getUrl);
        }

        /**
         * Helper method that uses the {@code file} extension to
         * determine if it's an image.
         */
        private boolean isImageFile(File file) {
            return file.isFile() &&
                    (file.getName().endsWith(".png")
                            || file.getName().endsWith(".jpg")
                            || file.getName().endsWith(".jpeg"));
        }
    }
}

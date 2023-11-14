package edu.vanderbilt.imagecrawler.utils;

/**
 * Custom exception for handling an invalid name of a transform
 * microservice or a problem with the microservice not running.
 */
public class TransformException
       extends RuntimeException {
    /**
     * Constructor passed the {@link String} {@code message}.
     *
     * @param message The reason the exception occurred
     */
    public TransformException(String message) {
        super(message);
    }
}

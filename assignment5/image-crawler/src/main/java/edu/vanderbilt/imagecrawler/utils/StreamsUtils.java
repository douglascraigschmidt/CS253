package edu.vanderbilt.imagecrawler.utils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.function.Predicate;

/**
 * A Java utility class containing helpful methods for manipulating
 * various modern Java Streams features.
 */
public final class StreamsUtils {
    /**
     * A utility class should define a private constructor.
     */
    private StreamsUtils() {
    }

    /**
     * Create a CompletableFuture that, when completed, will convert
     * all the completed CompletableFutures in the {@code futureList}
     * parameter into a list of joined results.
     *
     * @param futureArray A list of completable futures.
     * @return A CompletableFuture to a list that will contain all the
     *         joined results.
     */
    public static <T> CompletableFuture<List<T>>
        joinAll (List<CompletableFuture<T>> futureArray) {
        // Use CompletableFuture.allOf() to obtain a CompletableFuture
        // that will itself be complete when all CompletableFutures in
        // futureList parameter have completed.
        CompletableFuture<Void>
            allDoneFuture = CompletableFuture.allOf
            (futureArray.toArray(new CompletableFuture[futureArray.size()]));

        // When all futures have completed return a CompletableFuture to
        // a list of joined elements of type T.
        return allDoneFuture
            .thenApply(v -> {
                    // Create an array to store results.
                    List<T> results = new ArrayList<>();

                    futureArray
                        // Convert futureList into a stream of
                        // completable futures.
                        .stream()

                        // Use map() to join() all completablefutures
                        // and yield objects of type T.  Note that
                        // join() should never block.
                        .map(CompletableFuture::join)

                        // Add the results of type T into the array.
                        .forEach(results::add);

                    // Return the results array.
                    return results;
                });
    }

    /**
     * A generic negation predicate that can be used to negate a
     * predicate.
     *
     * @return The negation of the input predicate.
     */
    public static<T> Predicate<T> not(Predicate<T> p) {
        return p.negate();
    }

    /**
     * Maps the values of an Enum type to a corresponding array of
     * Strings.
     */
    public static String[] getNames(Class<? extends Enum<?>> e) {
        return Arrays
            .stream(e.getEnumConstants())
            .map(Enum::name)
            .toArray(String[]::new);
    }
}

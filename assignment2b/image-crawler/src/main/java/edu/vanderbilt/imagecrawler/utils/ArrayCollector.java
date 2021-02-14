package edu.vanderbilt.imagecrawler.utils;

import java.util.Collections;
import java.util.EnumSet;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.function.BinaryOperator;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collector;
import java.util.stream.Collectors;

import static edu.vanderbilt.imagecrawler.utils.Student.isGraduate;
import static edu.vanderbilt.imagecrawler.utils.Student.isUndergraduate;

/**
 * A mutable reduction operation that accumulates input elements
 * into a mutable result container.  These reduction operations
 * can be performed either sequentially or in parallel.
 */
public class ArrayCollector<T>
        implements Collector<T, Array<T>, Array<T>> {
    /**
     * This static factory method creates a new ArrayCollector.
     *
     * @return a new ArrayCollector()
     */
    public static <E> Collector<E, ?, Array<E>> toArray() {
        // TODO -- you fill in here replacing this statement with your solution.
        return new ArrayCollector<>();
    }

    /**
     * A function that creates and returns a new mutable result
     * container that will hold all the elements in the stream
     *
     * @return A function that returns a new, mutable result container
     */
    @Override
    public Supplier<Array<T>> supplier() {
        if (isGraduate()) {
            // TODO - Graduate students fill in here using a
            //  SynchronizedArray (replacing null with the proper code).
            return SynchronizedArray::new;
        } else if (isUndergraduate()) {
            // TODO - Undergraduate students fill in here using an
            //  UnsynchronizedArray (replacing null with the proper code).
            return UnsynchronizedArray::new;
        }

        throw new IllegalStateException("unreachable");
    }

    /**
     * A function that folds an Array into the mutable result
     * container.
     *
     * @return A function that folds a value into a mutable result container
     */
    @Override
    public BiConsumer<Array<T>, T> accumulator() {
        // TODO -- you fill in here replacing this statement with your solution.
        return Array::add;
    }

    /**
     * A function that accepts two partial results and merges them.
     * The combiner function may fold state from one argument into the
     * other and return that, or may return a new result container.
     *
     * @return A function that combines two partial results into a combined result
     */
    @Override
    public BinaryOperator<Array<T>> combiner() {
        if (isUndergraduate()) {
            // TODO -- you fill in here replacing this statement with your solution.
            return (left, right) -> {left.addAll(right);return left;};
        } else if (isGraduate()) {
            // Graduate students should not change this method.
            return null;
        }

        throw new IllegalStateException("unreachable");
    }

    /**
     * Perform the final transformation from the intermediate
     * accumulation type to the final result type.
     *
     * @return A function that transforms the intermediate result to the final result
     */
    @Override
    public Function<Array<T>, Array<T>> finisher() {
        if (isGraduate()) {
            // TODO - Graduate students fill in here (replacing null
            // with the proper code).
            return Array::toUnsynchronizedArray;
        } else if (isUndergraduate()) {
            // TODO - Undergraduate students fill in here (replacing
            // null with the proper code).
            return Function.identity();
        }

        throw new IllegalStateException("unreachable");
    }

    /**
     * Returns a {@code Set} of {@code Collector.Characteristics}
     * indicating the characteristics of this Collector.  This set
     * should be immutable.
     *
     * @return an immutable set of collector characteristics, which in
     * this case is UNORDERED and IDENTITY_FINISH for undergraduates
     * (graduates add CONCURRENT to these other two characteristics)
     */
    @Override
    public Set<Characteristics> characteristics() {
        if (isGraduate()) {
            // TODO - Graduate students fill in here (replacing null
            // with the proper code).
            return Collections.unmodifiableSet(EnumSet.of(
                    Collector.Characteristics.CONCURRENT,
                    Collector.Characteristics.UNORDERED,
                    Collector.Characteristics.IDENTITY_FINISH));
        } else if (isUndergraduate()) {
            // TODO - Undergraduate students fill in here (replacing
            // null with the proper code).
            return Collections.unmodifiableSet(EnumSet.of(
                    Collector.Characteristics.IDENTITY_FINISH));
        }

        throw new IllegalStateException("unreachable");
    }
}

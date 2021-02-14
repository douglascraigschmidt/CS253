package edu.vanderbilt.imagecrawler.utils;

import java.util.ArrayList;
import java.util.ConcurrentModificationException;
import java.util.Spliterator;
import java.util.Spliterators;
import java.util.function.Consumer;

/**
 * Defines an object for traversing and partitioning
 * elements of an Array.
 */
public final class ArraySpliterator<E>
        extends Spliterators.AbstractSpliterator<E> {
    /**
     * The array to traverse and/or partition.
     */
    // TODO (graduates 1b and undergraduates 2a) - you fill in here.
   private Array<E> array;

    /**
     * Current index, modified on advance/split.
     */
    // TODO (graduates 1b and undergraduates 2a) - you fill in here.
    
    private int index;
    /**
     * One past the end of the spliterator range.
     */
    // TODO (graduates 1b and undergraduates 2a) - you fill in here.
    
    private int fence;
    /**
     * Create new spliterator covering the given range.
     */
    ArraySpliterator(Array<E> array,
                     int origin,
                     int end) {
        super(array.size(),
                Spliterator.ORDERED | Spliterator.SIZED | Spliterator.SUBSIZED);

        // TODO (graduates 1b and undergraduates 2a) - you fill in here.
        this.array = array;
        this.index = origin;
        this.fence = end;
    }

    /**
     * If a remaining element exists, performs the given action on
     * it, returning true; else returns false.  Throw
     * NullPointerException of {@code action} is null.
     */
    public boolean tryAdvance(Consumer<? super E> action) {
        // TODO (graduates 1b and undergraduates 2a)
        //  you fill in here replacing this statement with your solution.
        if(action == null){
            throw new NullPointerException();
        }

        int hi = getFence(), i = index;
        if (i < hi) {
            index = i + 1;
            E e = (E)array.uncheckedToArray()[i];
            action.accept(e);

            return true;
        }
        return false;
    }

    /**
     * Returns a Spliterator covering elements that will, upon
     * return from this method, not be covered by this Spliterator.
     */
    public ArraySpliterator<E> trySplit() {
        // TODO (graduates 1b and undergraduates 2a)
        //  you fill in here replacing this statement with your solution.
        int hi = getFence(), lo = index, mid = (lo + hi) >>> 1;
        return (lo >= mid) ? null : // divide range in half unless too small
                new ArraySpliterator<E>(array, lo, index = mid);
    }

    private int getFence() { // initialize fence to size on first use
        int hi; // (a specialized variant appears in method forEach)
        Array<E> lst;
        if ((hi = fence) < 0) {
            if ((lst = array) == null)
                hi = fence = 0;
            else {
                hi = fence = lst.size();
            }
        }
        return hi;
    }
}

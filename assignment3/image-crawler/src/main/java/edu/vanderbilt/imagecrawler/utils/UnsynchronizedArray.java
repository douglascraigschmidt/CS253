package edu.vanderbilt.imagecrawler.utils;

import java.util.Arrays;
import java.util.Collection;
import java.util.ConcurrentModificationException;
import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.Spliterator;
import java.util.function.Consumer;
import java.util.function.UnaryOperator;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import static edu.vanderbilt.imagecrawler.utils.Assignment.Name.Assignment1a;
import static edu.vanderbilt.imagecrawler.utils.Assignment.Name.Assignment1b;
import static edu.vanderbilt.imagecrawler.utils.Assignment.Name.Assignment2a;
import static edu.vanderbilt.imagecrawler.utils.Assignment.isAssignment;
import static edu.vanderbilt.imagecrawler.utils.Assignment.isGraduate;
import static edu.vanderbilt.imagecrawler.utils.Assignment.isUndergraduate;

/**
 * A generic unsynchronized array class implemented via a single
 * contiguous buffer.
 */
@SuppressWarnings("ALL")
public class UnsynchronizedArray<E> implements Array<E> {
    /**
     * Default initial capacity (declared 'protected' for unit tests).
     */
    protected static final int DEFAULT_CAPACITY = 10;

    /**
     * Shared empty array instance used for empty instances.
     */
    private static final Object[] EMPTY_ELEMENTDATA = {};

    /**
     * The array buffer that stores all the array elements.  The
     * capacity is the length of this array buffer.
     */
    private Object[] mElementData;

    /**
     * The size of the array (the number of elements it contains).
     * This field also indicates the next "open" slot in the array,
     * i.e., where a call to add() will place the new element:
     * mElementData[mSize] = element.
     */
    protected int mSize;

    /*
     * The following methods and nested iterator class use Java 7 features.
     */

    /**
     * Constructs an empty array with an initial capacity of ten.
     */
    public UnsynchronizedArray() {
        mElementData = EMPTY_ELEMENTDATA;
    }

    /**
     * Constructs an empty array with the specified initial capacity.
     *
     * @param initialCapacity the initial capacity of the array
     * @throws IllegalArgumentException if the specified initial capacity
     *                                  is negative
     */
    public UnsynchronizedArray(int initialCapacity) {
        // TODO -- you fill in here.
        if (initialCapacity > 0) {
            this.mElementData = new Object[initialCapacity];
        } else if (initialCapacity == 0) {
            this.mElementData = EMPTY_ELEMENTDATA;
        } else {
            throw new IllegalArgumentException("Illegal Capacity: "+
                    initialCapacity);
        }
    }

    /**
     * Constructs a array containing the elements of the specified
     * collection, in the order they are returned by the collection's
     * iterator.
     *
     * @param c the collection whose elements are to be placed into this array
     * @throws NullPointerException if the specified collection is null
     */
    public UnsynchronizedArray(Collection<? extends E> c) {
        // TODO -- you fill in here.
        mElementData = c.toArray();
        if ((mSize = mElementData.length) != 0) {
            // c.toArray might (incorrectly) not return Object[] (see 6260652)
            if (mElementData.getClass() != Object[].class)
                mElementData = Arrays.copyOf(mElementData, mSize, Object[].class);
        } else {
            // replace with empty array.
            this.mElementData = EMPTY_ELEMENTDATA;
        }
    }

    /**
     * Returns <tt>true</tt> if this collection contains no elements.
     *
     * @return <tt>true</tt> if this collection contains no elements
     */
    public boolean isEmpty() {
        // TODO -- you fill in here (replace 'return false' with proper code).
        return mSize == 0;
    }

    /**
     * Returns the number of elements in this collection.
     *
     * @return the number of elements in this collection
     */
    public int size() {
        // TODO -- you fill in here (replace 'return 0' with proper code).
        return mSize;
    }

    /**
     * Returns the index of the first occurrence of the specified
     * element in this array, or -1 if this array does not contain the
     * element.
     *
     * @param o element to search for
     * @return the index of the first occurrence of the specified element in
     * this array, or -1 if this array does not contain the element
     */
    public int indexOf(Object o) {
        // TODO -- you fill in here (replace 'return -1' with proper code).
        if (o == null) {
            for (int i = 0; i < mSize; i++)
                if (mElementData[i]==null)
                    return i;
        } else {
            for (int i = 0; i < mSize; i++)
                if (o.equals(mElementData[i]))
                    return i;
        }
        return -1;
    }

    /**
     * Appends all of the elements in the specified collection to the
     * end of this array, in the order that they are returned by the
     * specified collection's Iterator.  The behavior of this
     * operation is undefined if the specified collection is modified
     * while the operation is in progress.  This implies that the
     * behavior of this call is undefined if the specified collection
     * is this array, and this array is nonempty.
     *
     * @param c collection containing elements to be added to this array
     * @return <tt>true</tt> if this array changed as a result of the call
     * @throws NullPointerException if the specified collection is null
     */
    public boolean addAll(Collection<? extends E> c) {
        // TODO -- you fill in here (replace 'return false' with proper code).
        Object[] a = c.toArray();
        int numNew = a.length;
        ensureCapacityInternal(mSize + numNew);  // Increments modCount
        System.arraycopy(a, 0, mElementData, mSize, numNew);
        mSize += numNew;
        return numNew != 0;
    }

    /**
     * Appends all of the elements in the specified Array to the end
     * of this array, in the order that they are returned by the
     * specified collection's Iterator.  The behavior of this
     * operation is undefined if the specified collection is modified
     * while the operation is in progress.  This implies that the
     * behavior of this call is undefined if the specified collection
     * is this array, and this array is nonempty.
     *
     * @param a array containing elements to be added to this array
     * @return <tt>true</tt> if this array changed as a result of the call
     * @throws NullPointerException if the specified collection is null
     */
    public boolean addAll(Array<E> a) {
        // TODO -- you fill in here (replace 'return false' with proper code).
        if(!a.isEmpty()){
            int numNew = a.size();
            ensureCapacityInternal(mSize + numNew);  // Increments modCount
            System.arraycopy(a, 0, mElementData, mSize, numNew);
            mSize += numNew;
            return numNew != 0;
        }
        return false;
    }

    /**
     * Removes the element at the specified position in this list.
     * Shifts any subsequent elements to the left (subtracts one from
     * their indices).
     *
     * @param index the index of the element to be removed
     * @return the element that was removed from the list
     * @throws IndexOutOfBoundsException
     */
    public E remove(int index) {
        // TODO -- you fill in here (replace 'return null' with proper code).
        rangeCheck(index);

        E oldValue = (E)mElementData[index];

        int numMoved = mSize - index - 1;
        if (numMoved > 0)
            System.arraycopy(mElementData, index+1, mElementData, index,
                    numMoved);
        mElementData[--mSize] = null; // clear to let GC do its work

        return oldValue;
    }

    /**
     * Checks if the given index is in range (i.e., index is
     * non-negative and it not equal to or larger than the size of the
     * Array) and throws the IndexOutOfBoundsException if it's not.
     * <p>
     * Normally should be declared as 'private', but for unit test access,
     * has been declared 'protected'.
     */
    @Override
    public void rangeCheck(int index) {
        // TODO -- you fill in here.
        if (index >= mSize || index < 0)
            throw new IndexOutOfBoundsException();
    }

    /**
     * Returns the element at the specified position in this array.
     *
     * @param index index of the element to return
     * @return the element at the specified position in this array
     * @throws IndexOutOfBoundsException
     */
    public E get(int index) {
        // TODO -- you fill in here (replace 'return null' with proper code).
        rangeCheck(index);
        return (E) mElementData[index];
    }

    /**
     * Replaces the element at the specified position in this list with
     * the specified element.
     *
     * @param index   index of the element to replace
     * @param element element to be stored at the specified position
     * @return the element previously at the specified position
     * @throws IndexOutOfBoundsException
     */
    public E set(int index, E element) {
        // TODO -- you fill in here (replace 'return null' with proper code).
        rangeCheck(index);
        E oldValue = (E)mElementData[index];
        mElementData[index] = element;
        return oldValue;
    }

    /**
     * Appends the specified element to the end of this array.
     *
     * @param element to be appended to this array
     * @return {@code true}
     */
    public boolean add(E element) {
        // TODO -- you fill in here (replace 'return false' with proper code).
        ensureCapacityInternal(mSize + 1);  // Increments modCount!!
        mElementData[mSize++] = element;
        return true;
    }

    /**
     * Ensure the array is large enough to hold {@code minCapacity}
     * elements.  The array will be expanded if necessary.
     * <p>
     * Normally should be declared as 'private', but for unit test access,
     * has been declared 'protected'.
     */
    protected void ensureCapacityInternal(int minCapacity) {
        // TODO -- you fill in here.
        ensureExplicitCapacity(calculateCapacity(mElementData, minCapacity));

    }
    private void ensureExplicitCapacity(int minCapacity) {

        // overflow-conscious code
        if (minCapacity - mElementData.length > 0)
            grow(minCapacity);
    }
    private void grow(int minCapacity) {
        // overflow-conscious code
        int oldCapacity = mElementData.length;
        int newCapacity = oldCapacity + (oldCapacity >> 1);
        if (newCapacity - minCapacity < 0)
            newCapacity = minCapacity;
        if (newCapacity - Integer.MAX_VALUE - 8 > 0)
            newCapacity = hugeCapacity(minCapacity);
        // minCapacity is usually close to size, so this is a win:
        mElementData = Arrays.copyOf(mElementData, newCapacity);
    }
    private static int hugeCapacity(int minCapacity) {
        if (minCapacity < 0) // overflow
            throw new OutOfMemoryError();
        return (minCapacity > Integer.MAX_VALUE - 8) ?
                Integer.MAX_VALUE :
                Integer.MAX_VALUE - 8;
    }

    private static int calculateCapacity(Object[] elementData, int minCapacity) {
        if (elementData == EMPTY_ELEMENTDATA) {
            return Math.max(DEFAULT_CAPACITY, minCapacity);
        }
        return minCapacity;
    }
    /**
     * @return a reference to the underlying unsynchronized array
     */
    public Array<E> toUnsynchronizedArray() {
        return this;
    }

    /**
     * @return a reference to the underlying buffer containing all of the elements in this Array
     * object in proper sequence
     */
    public Object[] uncheckedToArray() {
        return mElementData;
    }

    /**
     * Returns an array containing all of the elements in this Array
     * object in proper sequence (from first to last element).
     *
     * <p>The returned array will be "safe" in that no references to
     * it are maintained by this array.  (In other words, this method
     * must allocate a new array).  The caller is thus free to modify
     * the returned array.
     *
     * <p>This method acts as bridge between array-based and
     * collection-based APIs.
     *
     * @return an array containing all of the elements in this Array
     * object in proper sequence
     */
    public Object[] toArray() {
        return Arrays.copyOf(mElementData, mSize);
    }

    /**
     * Returns an array containing all of the elements in this list in
     * proper sequence (from first to last element); the runtime type
     * of the returned array is that of the specified array.  If the
     * list fits in the specified array, it is returned therein.
     * Otherwise, a new array is allocated with the runtime type of
     * the specified array and the size of this list.
     *
     * @param a the array into which the elements of the list are to
     *          be stored, if it is big enough; otherwise, a new array of the
     *          same runtime type is allocated for this purpose.
     * @return an array containing the elements of the list
     * @throws ArrayStoreException  if the runtime type of the specified array
     *                              is not a supertype of the runtime type of every element in
     *                              this list
     * @throws NullPointerException if the specified array is null
     */
    @SuppressWarnings("unchecked")
    public <T> T[] toArray(T[] a) {
        if (a.length < mSize) {
            // Make a new array of a's runtime type, but this array's contents.
            return (T[]) Arrays.copyOf(mElementData,
                    mSize,
                    a.getClass());
        }

        //noinspection SuspiciousSystemArraycopy
        System.arraycopy(mElementData, 0, a, 0, mSize);

        if (a.length > mSize) {
            a[mSize] = null;
        }

        return a;
    }

    /**
     * Returns an iterator over the elements in this Array in proper
     * sequence.
     *
     * @return an iterator over the elements in this Array in proper
     * sequence
     */
    public Iterator<E> iterator() {
        if ( isUndergraduate(Assignment1b)) {
            // TODO -- you fill in here replacing this statement with your solution.
            return new ArrayIterator();
        } else {
            return new ArrayIterator();
        }
    }

    /**
     * Replaces each element of this list with the result of applying
     * the operator to that element.  Errors or runtime exceptions
     * thrown by the operator are relayed to the caller.
     *
     * @param operator the operator to applyTransform to each element
     */
    public void replaceAll(UnaryOperator<E> operator) {
        // This method is a no-op in Assignment 1a.
        if (isAssignment(Assignment1b)) {
            // TODO - you fill in here (this implementation can use a for loop).
            for(int i = 0; i < mSize; i++){
                E oldValue = (E)mElementData[i];
                mElementData[i] = operator.apply(oldValue);
            }
        }
    }

    /*
     * The following methods and nested class use Java 8 features.
     */

    /**
     * Performs the given action for each element of the array until
     * all elements have been processed or the action throws an
     * exception.  Unless otherwise specified by the implementing
     * class, actions are performed in the order of iteration (if an
     * iteration order is specified).  Exceptions thrown by the action
     * are relayed to the caller.
     *
     * @param action The action to be performed for each element
     */
    public void forEach(Consumer<? super E> action) {
        if (isGraduate(Assignment1a)) {
            // TODO - Graduate students you fill in here
            //  using a for-each loop for assignment 1a.
            Iterator<E> it = iterator();
            while (it.hasNext()){
                action.accept(it.next());
            }


        } else if (isGraduate(Assignment1b)) {
            // TODO - Graduate students you fill in here using the
            //  Java stream forEach() method for assignment 1b.
            this.stream().forEach(action);

        } else if (isUndergraduate(Assignment1a)) {
            // TODO - Undergraduate students you fill in here using
            //  a simple for loop for assignment 1a.
            for(int i = 0; i < mSize; i++){
                action.accept((E)mElementData[i]);
            }
        } else if (isUndergraduate(Assignment1b)) {
            // TODO - Undergraduate students you fill in here
            //  using a for-each loop for assignment 1b.
            for(int i = 0; i < mSize; i++){
                action.accept((E)mElementData[i]);
            }
        } else {
            throw new IllegalStateException("unreachable");
        }
    }

    /**
     * Creates a {@link Spliterator} over the elements in the array.
     * The default method define here simply returns null.
     * <p>
     *
     * @return null (not implemented in this base class)
     */
    public Spliterator<E> spliterator() {
        if (isGraduate(Assignment1b)) {
            // TODO - you fill in here if your assignment matches either condition.
            return new ArraySpliterator<>(this, 0, -1);
        } else {
            throw new IllegalStateException("This exception should never occur.");
        }
    }

    /**
     * @return A parallel stream.
     */
    public Stream<E> parallelStream() {
        return StreamSupport.stream(spliterator(), true);
    }

    /**
     * @return A sequential stream.
     */
    public Stream<E> stream() {
        return StreamSupport.stream(spliterator(), false);
    }

    /**
     * This class defines an iterator over the elements in an Array in
     * proper sequence.
     */
    public class ArrayIterator implements Iterator<E> {
        /**
         * Current position in the Array (defaults to 0).
         */
        // TODO - you fill in here.

        int cursor;
        /**
         * Index of last element returned; -1 if no such element.
         */
        // TODO - you fill in here.
        int lastRet = -1;

        /**
         * @return True if the iteration has more elements that
         * haven't been iterated through yet, else false.
         */
        @Override
        public boolean hasNext() {
            // TODO - you fill in here (replace 'return false' with proper code).
            return cursor != mSize;
        }

        /**
         * @return The next element in the iteration.
         * @throws NoSuchElementException if there's no next element
         */
        @Override
        public E next() {
            // TODO - you fill in here (replace 'return null' with proper code).
            int i = cursor;
            if (mSize == 0 || i >= mSize)
                throw new NoSuchElementException();
            Object[] elementData = UnsynchronizedArray.this.mElementData;
            cursor = i + 1;
            return (E) elementData[lastRet = i];
        }

        /**
         * Removes from the underlying collection the last element
         * returned by this iterator. This method can be called only
         * once per call to next().
         *
         * @throws IllegalStateException if no last element was
         *                               returned by the iterator
         */
        @Override
        public void remove() {
            // TODO - you fill in here
            if (lastRet < 0)
                throw new IllegalStateException();

            try {
                UnsynchronizedArray.this.remove(lastRet);
                cursor = lastRet;
                lastRet = -1;
            } catch (IndexOutOfBoundsException ex) {
                throw new ConcurrentModificationException();
            }
        }
    }
}
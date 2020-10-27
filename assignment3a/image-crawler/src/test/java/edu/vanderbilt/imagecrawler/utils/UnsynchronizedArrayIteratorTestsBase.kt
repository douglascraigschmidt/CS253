package edu.vanderbilt.imagecrawler.utils

import admin.ArrayHelper
import admin.AssignmentTests
import org.junit.Assert.*
import java.lang.IllegalStateException
import java.util.*
import kotlin.NoSuchElementException

/**
 * Tests UnsynchronizedArray Iterator inner class.
 */
open class UnsynchronizedArrayIteratorTestsBase : AssignmentTests() {
    /**
     * A list with a similar content to mMixedInput but with all
     * entries shuffled.
     */
    private val mShuffledMixedInput = ArrayHelper.constructShuffledMixedInput()

    open fun `iterator must handle an empty array`() {
        val array = newArray<Any>()
        val iterator = array.iterator()
        assertFalse(iterator.hasNext())
    }

    open fun `iterator hasNext() and next() should work in conjunction to properly allow access to all array elements`() {
        val input = mShuffledMixedInput
        val array = newArray(input)

        val iterator = array.iterator()
        val expected = input.iterator()

        while (expected.hasNext() && iterator.hasNext()) {
            assertSame(expected.next(), iterator.next())
        }

        assertEquals(expected.hasNext(), iterator.hasNext())

        assertThrows(NoSuchElementException::class.java) {
            iterator.next()
        }
    }

    open fun `iterator next() must throw NoSuchElementException when array is empty`() {
        val array = newArray<Any>()
        assertThrows(NoSuchElementException::class.java) {
            array.iterator().next()
        }
    }

    open fun `iterator next() must throw NoSuchElementException for all calls after end of array is reached`() {
        val input = mShuffledMixedInput
        val array = newArray(input)
        val iterator = array.iterator()

        repeat(input.indices.count()) {
            assertTrue(iterator.hasNext())
            iterator.next()
        }

        assertFalse(iterator.hasNext())

        repeat(10) {
            assertThrows(NoSuchElementException::class.java) {
                iterator.next()
            }
        }
    }

    open fun `iterator add() and set() methods must work as expected`() {
        val input = ArrayList<Any?>()
        val pool = ArrayHelper.constructShuffledMixedInput()

        val array = newArray<Any>()

        // Start with empty array.
        assertEquals(input.iterator().hasNext(), array.iterator().hasNext())

        // Start with empty array.
        ArrayHelper.assertSameIteratorContents(input.iterator(), array.iterator())

        for (poolObject in pool) {
            // Test iterator after add operation.

            val tempObject = Any()
            input.add(tempObject)
            array.add(tempObject)

            ArrayHelper.assertSameIteratorContents(input.iterator(), array.iterator())
            ArrayHelper.assertSameContents(input.toTypedArray(), array.toArray())

            // Test iterator after set operation.

            val lastIndex = input.size - 1
            input[lastIndex] = poolObject
            array.set(lastIndex, poolObject)

            ArrayHelper.assertSameIteratorContents(input.iterator(), array.iterator())
            ArrayHelper.assertSameContents(input.toTypedArray(), array.toArray())
        }

        // Test iterator after removing items.

        val random = Random()

        while (input.size > 0) {
            // Test iterator after add operation.

            val randomIndex = random.nextInt(input.size)
            input.removeAt(randomIndex)
            array.remove(randomIndex)

            ArrayHelper.assertSameIteratorContents(input.iterator(), array.iterator())
        }

        assertThrows(NoSuchElementException::class.java) {
            array.iterator().next()
        }
    }

    open fun `iterator remove() must be able to remove all elements`() {
        val input = mShuffledMixedInput.toMutableList()
        val array = newArray(input)

        ArrayHelper.assertSameIteratorContents(input.iterator(), array.iterator())

        val expected = input.iterator()
        val iterator = array.iterator()

        while (expected.hasNext() && iterator.hasNext()) {
            val objectExpected = expected.next()
            val `object` = iterator.next()
            assertSame(objectExpected, `object`)
            expected.remove()
            iterator.remove()
            ArrayHelper.assertSameContents(input.toTypedArray(), array.toArray())
            assertEquals(input.size.toLong(), array.size().toLong())
        }

        assertEquals(expected.hasNext(), iterator.hasNext())
        assertEquals(0, input.size.toLong())
        assertEquals(0, array.size().toLong())
    }

    open fun `iterator remove() throws and exception if called twice in a row`() {
        val input = mShuffledMixedInput.toMutableList()
        val array = newArray(input)
        ArrayHelper.assertSameIteratorContents(input.iterator(), array.iterator())
        val iterator = array.iterator()
        assertTrue(iterator.hasNext())
        iterator.next()
        iterator.remove()
        assertThrows(IllegalStateException::class.java) {
            iterator.remove()
        }
    }

    private fun <T> newArray(input: List<T>): Array<T> {
        return UnsynchronizedArray(input)
    }

    private fun <T> newArray(): Array<T> {
        return UnsynchronizedArray()
    }
}

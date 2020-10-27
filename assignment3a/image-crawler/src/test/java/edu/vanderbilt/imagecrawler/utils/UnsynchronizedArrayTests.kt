package edu.vanderbilt.imagecrawler.utils

import admin.*
import admin.ArrayHelper.ObjectIsEqualButIsNotSame
import admin.ArrayHelper.assertSameContents
import admin.ArrayHelper.assertSameContentsAndLength
import admin.ArrayHelper.constructInputWithObjectsSameButNotEqual
import admin.ArrayHelper.constructMixedInput
import admin.ArrayHelper.constructShuffledMixedInput
import admin.ArrayHelper.getElements
import admin.ArrayHelper.getInputNoDups
import admin.ArrayHelper.setSize
import edu.vanderbilt.imagecrawler.crawlers.CustomArray
import edu.vanderbilt.imagecrawler.utils.Assignment.Name.Assignment1a
import edu.vanderbilt.imagecrawler.utils.Assignment.Name.Assignment1b
import edu.vanderbilt.imagecrawler.utils.Student.Type.Graduate
import edu.vanderbilt.imagecrawler.utils.Student.Type.Undergraduate
import io.mockk.*
import org.junit.Assert.*
import org.junit.Test
import java.util.*
import java.util.function.Consumer

/**
 * Test program for the Java 7 features of the Array class.
 * Note that since the sample input data contains objects whose
 * equals() methods always returns false, care must be used to
 * use assertSame() instead of assertEquals() where necessary.
 */
class UnsynchronizedArrayTests : AssignmentTests() {
    /**
     * Maximum test size (must be an even value).
     */
    private val mMaxTestSize = 20

    /**
     * A list containing:
     *
     * 1. Unique Object instances.
     * 2. Duplicate Object instances that will return true when
     *    compared with both equals() and ==
     * 3. Two or more duplicated ObjectIsSameButNotEqual instances
     *    which is a special class I've made that always returns
     *    false for equals() and true for ==. These objects will
     *    help detect cases where == is used instead of equals().
     * 4. One or more null values.
     * 5. Two or more ObjectIsEqualButNotSame instances that will
     *    return true when compared with equals() but false when
     *    compared with ==.
     */
    private val mixedInput = constructMixedInput()

    /**
     * A list with a similar content to mMixedInput but with all
     * entries shuffled.
     */
    private val shuffledMixedInput = constructShuffledMixedInput()

    /**
     * A special list to ensure that Array implementation always uses
     * equals() method for comparisons and not the sameness operator ==.
     *
     * It contains:
     *
     * Two or more ObjectIsEqualButNotSame instances that will
     * return true when compared with equals() but false when
     * compared with ==.
     */
    private val mInputSameButNotEqual = constructInputWithObjectsSameButNotEqual()

    /**
     * A list of mMaxTestSize random integers (with no duplicates).
     */
    private val mInputWithNoDups = getInputNoDups(mMaxTestSize)

    @Test
    fun `constructor must handle all valid capacity inputs`() {
        for (i in 0..mMaxTestSize) {
            val array = newArray<Int>(i)
            val data = getElements(array)
            assertNotNull(data)
            assertEquals(data.size, i)
            assertEquals(0, array.size())
        }
    }

    //--------------------------------------------------------------------------
    // Tests
    //--------------------------------------------------------------------------

    /**
     * This test should always pass, even on an empty skeleton since it's purpose
     * is to ensure that the student does not change or alter the default constructor.
     */
    @Test
    fun `test default constructor`() {
        val array = UnsynchronizedArray<Any?>()
        val elements = getElements(array)
        assertNotNull(elements)

        val expected = array.getField<kotlin.Array<Any>>("EMPTY_ELEMENTDATA")

        assertSame(expected, elements)
        assertEquals(0, elements.size)
        assertEquals(0, array.size())
    }

    /**
     * Test constructors.
     */
    @Test
    fun `constructor must handle non-empty collection input`() {
        val input = shuffledMixedInput
        val array = UnsynchronizedArray(input)
        val data = getElements(array)

        assertNotNull(data)
        assertEquals(input.size, data.size)
        assertEquals(input.size, array.size())
        for (i in input.indices) {
            // Use same() and not equals() to ensure that
            // array has the same objects and not clones.
            assertSame(input[i], data[i])
        }
    }

    @Test
    fun `constructor must handle empty collection input`() {
        val emptyCollection = ArrayList<Any>(0)
        val array = UnsynchronizedArray(emptyCollection)
        val data = getElements(array)

        assertNotNull(data)
        assertEquals(0, array.size())
        assertEquals(0, data.size)
    }

    @Test
    fun `constructor must handle an invalid capacity input`() {
        assertThrows(IllegalArgumentException::class.java) {
            UnsynchronizedArray<Any>(-1)
        }
    }

    @Test
    fun `constructor must handle an invalid collection input`() {
        assertThrows(NullPointerException::class.java) {
            UnsynchronizedArray<Any>(null)
        }
    }

    /**
     * Test size method.
     */

    @Test
    fun `size() black box test`() {
        val input = shuffledMixedInput
        val array = UnsynchronizedArray(input)
        assertEquals(input.size, array.size())
    }

    /**
     * This test will catch cases where mElementData is
     * needlessly referenced in size() method.
     */
    @Test
    fun `size() method must only use size property`() {
        val input = shuffledMixedInput
        val array = newArray(input)

        array.setField<kotlin.Array<Any>>("mElementData", arrayOf(1, 2, 3, 4))
        array.setField("mSize", 1, Int::class.javaPrimitiveType)

        assertEquals(1, array.size())

        array.setField<kotlin.Array<Any>>("mElementData", null)
        array.setField("mSize", 4, Int::class.javaPrimitiveType)

        assertEquals(4, array.size())
    }

    /**
     * Test isEmpty method.
     */

    @Test
    fun `isEmpty() must return true when array has a zero capacity`() {
        val array = newArray<Any>(0)
        // First make sure that constructor actually worked as expected.
        assertNotNull(getElements(array))
        assertEquals(0, getElements(array).size)

        // Test method.
        assertTrue(array.isEmpty)
    }

    @Test
    fun `isEmpty() should return true when a the capacity is greater than 0 but no elements exist`() {
        val array = newArray<Any>(1)
        assertEquals(1, getElements(array).size)

        // Test method.
        assertTrue(array.isEmpty)
    }

    /**
     * This test will catch cases where mElementData is
     * needlessly referenced in isEmpty() method.
     */
    @Test
    @Throws(Exception::class)
    fun `isEmpty() white box test`() {
        val input = shuffledMixedInput
        val array = newArray(input)
        assertEquals(input.size, array.size())

        array.setField<kotlin.Array<Any>>("mElementData", null)
        array.setField("mSize", 1, Int::class.javaPrimitiveType)

        assertFalse(array.isEmpty)
    }

    /**
     * Test rangeCheck method
     */

    @Test
    @Throws(IllegalAccessException::class)
    fun `rangeCheck() should only use size member`() {
        val fakeSize = 10
        val array = newArray<Any>(fakeSize)
        assertEquals(10, getElements(array).size)

        // Set size to an arbitrary value even though there
        // are no backing elements in this empty array; rangeCheck
        // should not only care about mSize and not the contents of
        // the backing element data.
        setSize(array, fakeSize)

        for (i in 0 until fakeSize) {
            // Should never throw an exception.
            array.rangeCheck(i)
        }
    }

    @Test
    fun `rangeCheck() should throw IndexOutOfBoundsException when array is empty`() {
        assertThrows(IndexOutOfBoundsException::class.java) {
            newArray<Any>().rangeCheck(0)
        }
    }

    @Test
    fun `rangeCheck() should throw IndexOutOfBounds exception for a negative index`() {
        assertThrows(IndexOutOfBoundsException::class.java) {
            newArray(mixedInput).rangeCheck(-1)
        }
    }

    @Test
    fun `rangeCheck() should throw IndexOutOfBounds exception for an index that exceeds the upper bound`() {
        assertThrows(IndexOutOfBoundsException::class.java) {
            newArray(mixedInput).rangeCheck(mixedInput.size)
        }
    }

    /**
     * Test indexOf method
     */

    @Test
    fun `indexOf() black box test`() {
        val input = mInputWithNoDups
        val array = newArray(input)
        assertEquals(input.size, array.size())

        for (i in input.indices) {
            val obj = input[i]
            val indexExpected = input.indexOf(obj)
            val indexReturned = array.indexOf(obj)
            assertSame(indexExpected, indexReturned)
        }

        assertEquals(array.indexOf(null), -1)
    }

    /**
     * Since the shuffled mixed input is used, this is a
     * full coverage test that covers all possible cases
     * of the indexOf() method (nulls, duplicates, clones,
     * objects where equals() always returns false).
     */
    @Test
    fun `indexOf() must handle all possible input variations`() {
        val input = shuffledMixedInput
        val array = newArray(input)
        assertEquals(input.size, array.size())

        for (i in input.indices) {
            val obj = input[i]
            val indexExpected = input.indexOf(obj)
            val indexReturned = array.indexOf(obj)
            assertSame(indexExpected, indexReturned)
        }
    }

    /**
     * This test only checks that the indexOf uses equals and not ==.
     */
    @Test
    fun `indexOf() must use equals() and not == operator`() {
        val input = mInputSameButNotEqual
        val array = newArray(input)
        assertEquals(input.size, array.size())

        for (i in input.indices) {
            val obj = input[i]

            // Sanity check - should be same but not equal.
            assertSame(obj, obj)
            assertNotEquals(obj, obj)

            // Now test for expected value.
            assertEquals(-1, array.indexOf(obj))
        }

        assertEquals(array.indexOf(null), -1)
    }

    /**
     * Test indexOf() for false positives (matches).
     */
    @Test
    fun `indexOf() must only return -1 for input objects that don't exist in the array`() {
        val input = mixedInput
        val array = newArray(input)
        assertEquals(input.size, array.size())

        // Note that nulls and ObjectIsEqualButIsNotSame objects will
        // match, so we ignore those when looking for false positives.
        val nonExistentInput = constructMixedInput()

        for (i in nonExistentInput.indices) {
            val obj = nonExistentInput[i]
            val indexReturned = array.indexOf(obj)
            if (obj == null || obj is ObjectIsEqualButIsNotSame) {
                assertNotEquals(-1, indexReturned)
            } else {
                assertEquals(-1, indexReturned)
            }
        }
    }

    /**
     * Test add addAll(Collection) method.
     */

    @Test
    fun `addAll(Collection) must throw a NullPointerException when input is a null collection`() {
        val array = newArray<Any>()
        assertThrows(NullPointerException::class.java) {
            array.addAll(null as Collection<*>?)
        }
    }

    @Test
    fun `addAll(Collection) must handle a mixed input collection`() {
        val input = shuffledMixedInput
        val array = newArray<Any>(0)
        assertEquals(0, array.size())

        // The test
        assertTrue(array.addAll(input))

        assertEquals(input.size, array.size())

        for (i in input.indices) {
            val obj = input[i]
            assertSame(obj, array.get(i))
        }
    }

    @Test
    fun `addAll(Collection) must append an input collection to the end of the array`() {
        val input = shuffledMixedInput.toMutableList()
        val array = newArray(input)
        assertEquals(input.size, array.size())

        val inputToAppend = constructShuffledMixedInput()

        // The test
        assertTrue(array.addAll(inputToAppend))

        // Match the change in our input for later comparison/verification.
        input.addAll(inputToAppend)
        assertEquals(input.size, array.size())

        for (i in input.indices) {
            val obj = input[i]
            assertSame(obj, array.get(i))
        }
    }

    @Test
    fun `addAll(Collection) must not change array when input is an empty collection`() {
        val input = shuffledMixedInput
        val array = newArray(input)
        assertEquals(input.size, array.size())

        // Capture backing data array to ensure that it is not
        // modified when addAll is called with an empty collection.
        val elementsBefore = getElements(array)
        val expectedLength = elementsBefore.size

        val emptyInput = ArrayList<Any>(10)

        // The test
        assertFalse(array.addAll(emptyInput))

        // Nothing should change
        assertEquals(input.size, array.size())
        val elementsAfter = getElements(array)

        // Should be the same backing object.
        assertSame(elementsBefore, elementsAfter)

        // Backing object length should not have changed.
        assertEquals(expectedLength, elementsAfter.size)
    }

    /**
     * Test add addAll(Array) method.
     */

    @Test
    fun `addAll(Array) must throw a NullPointerException when input is a null Collection`() {
        val array = newArray<Any>()
        assertThrows(NullPointerException::class.java) {
            array.addAll(null as Collection<*>?)
        }
    }

    @Test
    fun `addAll(Array) must handle a mixed input Array`() {
        val input = shuffledMixedInput
        val array = newArray<Any>(0)
        assertEquals(0, array.size())

        // The test
        assertTrue(array.addAll(input))

        assertEquals(input.size, array.size())

        for (i in input.indices) {
            val obj = input[i]
            assertSame(obj, array.get(i))
        }
    }

    @Test
    fun `addAll(Array) must append input to the end of the array`() {
        val input = shuffledMixedInput.toMutableList()
        val array = newArray(input)
        assertEquals(input.size, array.size())

        val inputToAppend = constructShuffledMixedInput()
        array.addAll(inputToAppend)

        input.addAll(inputToAppend)
        assertEquals(input.size, array.size())

        for (i in input.indices) {
            val obj = input[i]
            assertSame(obj, array.get(i))
        }
    }

    @Test
    fun `addAll(Array) must not change array when input is an empty array`() {
        val input = shuffledMixedInput
        val array = newArray(input)
        assertEquals(input.size, array.size())

        // Capture backing data array to ensure that it is not
        // modified when addAll is called with an empty collection.
        val elementsBefore = getElements(array)
        val expectedLength = elementsBefore.size

        val emptyInput = newArray<Any>(10)

        // The test
        assertFalse(array.addAll(emptyInput))

        // Nothing should change
        assertEquals(input.size, array.size())
        val elementsAfter = getElements(array)

        // Should be the same backing object.
        assertSame(elementsBefore, elementsAfter)

        // Backing object length should not have changed.
        assertEquals(expectedLength, elementsAfter.size)
    }

    @Test
    fun `addAll(Array) should not call inefficient toArray() method`() {
        val input = spyk(newArray<Any?>(shuffledMixedInput))
        val array = newArray<Any?>(0)
        array.addAll(input)
        verify(exactly = 0) { input.toArray() }
    }

    /**
     * Test remove method.
     */

    @Test
    fun `test remove() method`() {
        val input = shuffledMixedInput.toMutableList()
        val array = spyk(newArray(input))
        assertEquals(input.size, array.size())

        val random = Random()
        while (input.size > 0) {
            // Reset spy so that we can match a single call to rangeCheck().

            clearMocks(array)

            val index = random.nextInt(input.size)
            val expectedObject = input.removeAt(index)
            val returnedObject = array.remove(index)

            assertEquals(input.size, array.size())
            assertSame(expectedObject, returnedObject)
            assertEquals(input.size, array.size())

            // Check for the required rangeCheck call.
            verify(exactly = 1) { array.rangeCheck(index) }

            for (i in input.indices) {
                assertSame(input[i], array.get(i))
            }
        }

        assertEquals(0, input.size)
        assertEquals(0, array.size())
    }

    @Test
    fun `remove() must throw IndexOutOfBoundsException when removing from an empty array`() {
        assertThrows(IndexOutOfBoundsException::class.java) {
            newArray<Any>().remove(0)
        }
    }

    @Test
    fun `remove() must throw IndexOutOfBoundsException when input index is negative`() {
        assertThrows(IndexOutOfBoundsException::class.java) {
            newArray(mixedInput).remove(-1)
        }
    }

    @Test
    fun `remove() must throw IndexOutOfBoundsException when input index exceeds array upper bound`() {
        assertThrows(IndexOutOfBoundsException::class.java) {
            newArray(mixedInput).remove(mixedInput.size)
        }
    }

    /**
     * Test get method.
     */

    @Test
    fun `test get() method`() {
        val input = shuffledMixedInput
        val array = spyk(newArray(input))
        assertEquals(input.size, array.size())

        for (i in input.indices) {
            val expected = input[i]
            val result = array.get(i)
            assertSame(expected, result)
        }

        // Check for the required number of rangeCheck calls.
        verify(exactly = input.size) { array.rangeCheck(any()) }
    }

    @Test
    fun `get() must throw IndexOutOfBoundsException when array is empty`() {
        assertThrows(IndexOutOfBoundsException::class.java) {
            newArray<Any>().get(0)
        }
    }

    @Test
    fun `get() must throw IndexOutOfBoundsException when input index is negative`() {
        assertThrows(IndexOutOfBoundsException::class.java) {
            newArray(mixedInput).get(-1)
        }
    }

    @Test
    fun `get() must throw IndexOutOfBoundsException when input index exceeds array upper bound`() {
        assertThrows(IndexOutOfBoundsException::class.java) {
            newArray(mixedInput).get(mixedInput.size)
        }
    }

    /**
     * Test set method.
     */

    @Test
    fun `test set() method`() {
        val input = constructShuffledMixedInput().toMutableList()
        val pool = constructShuffledMixedInput()

        val array = spyk(newArray(input, 0))
        assertEquals(input.size, array.size())

        for (i in input.indices) {
            val setObject = pool[i]
            val expectedOldValue = input.set(i, setObject)
            val returnedOldValue = array.set(i, setObject)
            assertSame(expectedOldValue, returnedOldValue)
            assertSameContentsAndLength(input.toTypedArray(), getElements(array))
        }

        assertSameContentsAndLength(
                pool.toTypedArray(), getElements(array))

        // Check for the required number of rangeCheck calls.
        verify(exactly = pool.size) { array.rangeCheck(any()) }
    }

    @Test
    fun `set() must throw IndexOutOfBoundsException when array is empty`() {
        assertThrows(IndexOutOfBoundsException::class.java) {
            newArray<Any>().set(0, null)
        }
    }

    @Test
    fun `set() must throw IndexOutOfBoundsException when input index is negative`() {
        assertThrows(IndexOutOfBoundsException::class.java) {
            newArray(mixedInput).set(-1, null)
        }
    }

    @Test
    fun `set() must throw IndexOutOfBoundsException when input index exceeds array upper bound`() {
        assertThrows(IndexOutOfBoundsException::class.java) {
            newArray(mixedInput).set(mixedInput.size, null)
        }
    }

    /**
     * Test ensureCapacity method.
     */

    @Test
    fun `ensureInternalCapacity() should only reallocate data members when absolutely necessary`() {
        val array = newArray<Any>()

        // First call to ensure an initial capacity.
        array.ensureCapacityInternal(1)
        val expected = getElements(array)
        assertTrue(expected.isNotEmpty())

        // Second call with same capacity should produce no change.
        array.ensureCapacityInternal(1)
        val objects = getElements(array)

        // Backing data should not have changed in any way.
        assertSame(expected, objects)

        // Array size should also remain at 0.
        assertEquals(0, array.size())
    }

    @Test
    fun `ensureInternalCapacity() should efficiently handle capacity input in range 1 to 100`() {
        val array = newArray<Any>()

        val maxCapacity = 100
        for (i in 0 until maxCapacity) {
            // Test call.
            array.ensureCapacityInternal(i)
            val expectedElementsArray = getElements(array)
            assertTrue(expectedElementsArray.size >= i)
            assertEquals(0, array.size())

            // Backing array should not change when subsequent calls
            // to ensureCapacityInternal() are called with sizes less
            // than the current capacity.
            for (j in 0..i) {
                // Test call.
                array.ensureCapacityInternal(j)
                val elementsArray = getElements(array)
                assertSame(expectedElementsArray, elementsArray)
                assertEquals(0, array.size())
            }
        }
    }

    @Test
    fun `ensureInternalCapacity() must allocate memory efficiently`() {
        val array = newArray<Any>()
        val minAllocs = 2
        val maxAllocs = 13
        var allocs = 0
        val emptyArray = getStaticValue<UnsynchronizedArray<Any>, kotlin.Array<Any>>("EMPTY_ELEMENTDATA")

        assertSame(emptyArray, getElements(array))

        for (i in 0..999) {
            val oldData = getElements(array)
            array.ensureCapacityInternal(i)
            if (i > 0) {
                assertNotSame(emptyArray, getElements(array))
            }
            val newData = getElements(array)
            if (oldData !== newData) {
                allocs++
                if (allocs == 1) {
                    assertEquals(UnsynchronizedArray.DEFAULT_CAPACITY, newData.size)
                }
            }
        }

        assertTrue("$allocs memory allocation(s) occurred for parameters [1..1000]; " +
                "allocation algorithm should perform more memory allocations!",
                allocs >= minAllocs)

        assertTrue("$allocs memory allocations occurred for parameters [1..1000]; " +
                "allocation algorithm should be more efficient (maximum of $maxAllocs allocations)!",
                allocs <= maxAllocs)
    }

    @Test
    fun `ensureInternalCapacity() must always copy input data reallocating storage`() {
        val input = shuffledMixedInput
        val array = UnsynchronizedArray(input)
        assertSameContents(input.toTypedArray(), array.toArray())

        for (i in 0..999) {
            val oldData = getElements(array)
            array.ensureCapacityInternal(i)
            val newData = getElements(array)
            if (oldData !== newData) {
                assertSameContents(input.toTypedArray(), newData)
                break
            }
        }
    }

    @Test
    fun `test add() method`() {
        val expected = shuffledMixedInput.toMutableList()
        val array = spyk(newArray(expected), recordPrivateCalls = true)

        assertSameContents(expected.toTypedArray(), array.toArray())

        var addCalls = 0

        for (i in 0..99) {
            val newObjects = constructShuffledMixedInput()

            for (obj in newObjects) {
                expected.add(obj)
                array.add(obj)
                addCalls++
            }

            val elements = getElements(array)
            assertSameContents(expected.toTypedArray(), elements)
            assertEquals(expected.size, array.size())
        }

        verify(exactly = addCalls) { array.ensureCapacityInternal(any()) }
    }

    @Test
    fun `forEach() must work as expected for graduate assignment1a solution`() {
        runAs(Graduate, Assignment1a)

        forEachBaseTest { array ->
            verify(exactly = 0) { array.stream() }
            verify(exactly = 1) { array.iterator() }
        }
    }

    @Test
    fun `forEach() must work as expected for graduate assignment1b solution`() {
        runAs(Graduate, Assignment1b)

        forEachBaseTest { array ->
            verify(exactly = 1) { array.stream() }
            verify(exactly = 0) { array.iterator() }
        }
    }

    @Test
    fun `forEach() must work as expected for undergraduate Assignment1a solution`() {
        runAs(Undergraduate, Assignment1a)

        forEachBaseTest { array ->
            verify(exactly = 0) { array.stream() }
            verify(exactly = 0) { array.iterator() }
        }
    }

    @Test
    fun `forEach() must work as expected for undergraduate Assignment1b solution`() {
        runAs(Undergraduate, Assignment1b)

        forEachBaseTest { array ->
            verify(exactly = 0) { array.stream() }
            verify(exactly = 1) { array.iterator() }
        }
    }

    private fun forEachBaseTest(block: (CustomArray<Any?>) -> Unit) {
        val expected = newArray(shuffledMixedInput)
        val array = spyk(newArray(shuffledMixedInput))
        val result = newArray<Any?>()

        var calls = 0
        val maxCalls = expected.getJavaPrimitiveField<Int>("mSize", Int::class.java)

        array.forEach(Consumer {
            if (++calls > maxCalls) {
                fail("You should not use mElementData in your forEach solution!")
            }
            result.add(it)
        })

        // Ensure that a1 hasn't changed.
        assertArrayEquals(expected.toArray(), array.toArray())

        // a2 should have the same contents and size as a1.
        assertArrayEquals(array.toArray(), result.toArray())

        verify(exactly = 0) { array.set(any(), any()) }

        block(array)
    }

    @Test
    fun `replaceAll() must work as expected`() {
        runAs(Assignment1b)
        val size = 66

        val expected = (1..size).toMutableList()
        val array = spyk(newArray<Int>())
        repeat(size) {
            array.add(it + 1)
        }
        var calls = 0
        val maxCalls = expected.size

        // Mimic with ArrayList.
        expected.replaceAll {
            it + 1
        }

        // Test method.
        array.replaceAll {
            if (++calls > maxCalls) {
                fail("Your solution iterates past then end of the mElementData array!")
            }
            it + 1
        }

        verify(exactly = 0) { array.iterator() }
        verify(exactly = 0) { array.get(any()) }
        verify(exactly = 0) { array.set(any(), any()) }

        assertArrayEquals(expected.toTypedArray(), array.toArray())
    }

    private fun <T> newArray(size: Int) = UnsynchronizedArray<T>(size)

    private inline fun <reified T> newArray(input: List<T?>? = null, padding: Int = 10) =
            if (input == null) {
                UnsynchronizedArray()
            } else {
                // Always increase list size to catch overruns.
                val size = input.size + padding
                val expandedList = MutableList(size) {
                    if (it < input.size) input[it] else null
                }
                UnsynchronizedArray<T>().also {
                    it.setField("mElementData", expandedList.toTypedArray())
                    assertEquals(size, it.getField<kotlin.Array<T>>("mElementData").size)
                    // truncate to catch overruns
                    it.setJavaPrimitiveField("mSize", input.size)
                }
            }
}

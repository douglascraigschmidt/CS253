package admin

import admin.ReflectionHelper.findFirstMatchingFieldValue
import admin.ReflectionHelper.injectValueIntoMatchingField
import edu.vanderbilt.imagecrawler.crawlers.CustomArray
import junit.framework.TestCase
import org.junit.Assert.*
import java.util.*
import java.util.Collections.shuffle

object ArrayHelper {
    /**
     * Constructs a sample input data list that contains a mix of object
     * types and duplication styles.
     */
    fun constructMixedInput(): List<Any?> =
            // Create a list with 5 objects each of which has a single duplicate.
            constructObjects(5, 2).apply {
                // Add a couple of null values.
                addAll(constructDuplicates(null, 2))
                // Add Special objects that are the same but are not equal.
                addAll(constructInputWithObjectsSameButNotEqual())
                // Add Special objects that are equal but are not the same.
                addAll(constructInputWithObjectsEqualButNotSame())
            }

    fun constructInputWithObjectsSameButNotEqual(): List<Any?> {
        // Add two occurrences of a single ObjectIsSameButNotEqual instance
        // which will return true for == comparison, but false for equals().
        val objects = constructDuplicates(ObjectIsSameButNotEqual(), 2)
        val index = 0

        // Make sure the ObjectIsSameButNotEqual class behaves in the expected manner.
        assertSame(objects[index], objects[index + 1])
        assertNotEquals(objects[index], objects[index + 1])
        assertNotEquals(objects[index + 1], objects[index])
        return objects
    }

    fun constructInputWithObjectsEqualButNotSame(): List<Any?> {
        // Add a single long value that is contained in two boxed Long instances so
        // that when compared by == will return false, and when compared by equals()
        // will return true.
        val objects: MutableList<Any?> = ArrayList(2)
        objects.add(ObjectIsEqualButIsNotSame(1))
        objects.add(ObjectIsEqualButIsNotSame(1))
        val index = 0

        // Ensure expected behaviour of ObjectIsEqualButNotSame instances.
        assertNotSame(objects[index], objects[index + 1])
        assertEquals(objects[index], objects[index + 1])
        assertEquals(objects[index + 1], objects[index])
        return objects
    }

    fun getInputNoDups(size: Int): List<Any> = (0..size).toList().shuffled()

    fun constructDuplicates(obj: Any?, duplicates: Int): List<Any?> {
        val objects: MutableList<Any?> = ArrayList()
        for (i in 0 until duplicates) {
            objects.add(obj)
        }
        assertEquals(duplicates, objects.size)
        return objects
    }

    fun constructObjects(size: Int, duplicates: Int): MutableList<Any?> {
        val objects: MutableList<Any?> = ArrayList()
        for (i in 0 until size) {
            objects.addAll(constructDuplicates(Any(), duplicates))
        }
        assertEquals(size * duplicates.toLong(), objects.size.toLong())
        return objects
    }

    fun constructShuffledMixedInput(): List<Any?> {
        val mixedInput = constructMixedInput()
        shuffle(mixedInput)
        return mixedInput
    }

    /**
     * Helper that uses reflection to access the contents
     * of the private Object[] field "mElementData".
     */
    fun getElements(array: CustomArray<*>?): Array<Any?> {
        return findFirstMatchingFieldValue(
                array,
                Array<Any>::class.java,
                "mElementData")
    }

    /**
     * Helper that uses reflection to set the contents
     * of the private Object[] field "mElementData".
     */
    fun CustomArray<Any>.setElements(elements: Array<Any>) {
        setField("mElementData", elements)
    }

    /**
     * Helper that uses reflection to access the contents
     * of the private "mSize" int field.
     */
    fun <T> CustomArray<T>.getSize() = getField<Int>("mSize")

    /**
     * Helper that uses reflection to set the contents
     * of the private Object[] field "mElementData".
     */
    fun setSize(array: CustomArray<*>?, size: Int) {
        injectValueIntoMatchingField(
                array, size, Int::class.javaPrimitiveType, "mSize")
    }

    /**
     * Helper that compares contents of two arrays for sameness (== operator).
     */
    fun assertSameContents(expected: Array<Any?>, objects: Array<Any?>) {
        for (i in expected.indices) {
            assertSame(expected[i], objects[i])
        }
    }

    /**
     * Helper that compares contents of two arrays for sameness (== operator).
     */
    fun assertSameContentsAndLength(expected: Array<Any?>, objects: Array<Any?>) {
        assertEquals(expected.size.toLong(), objects.size.toLong())
        assertSameContents(expected, objects)
    }

    /**
     * Helper that compares 2 iterators for same content.
     */
    fun assertSameIteratorContents(expected: Iterator<*>, iterator: Iterator<*>) {
        while (expected.hasNext() && iterator.hasNext()) {
            assertSame(expected.next(), iterator.next())
        }
        assertEquals(expected.hasNext(), iterator.hasNext())
        try {
            iterator.next()
            TestCase.fail("Expected NoSuchElementException")
        } catch (e: NoSuchElementException) {
            // Expected
        } catch (t: Throwable) {
            TestCase.fail("Expected NoSuchElementException but instead got " + t.javaClass.name)
        }
    }

    /**
     * Helper that compares contents of two arrays for sameness (== operator).
     */
    fun assertArrayEquals(expected: CustomArray<Any?>, array: CustomArray<Any?>) {
        assertEquals(expected.size().toLong(), array.size().toLong())
        for (i in 0 until expected.size()) {
            assertSame(expected[i], array[i])
        }
    }

    /**
     * Special test object that has no equal object instances.
     * Note the equals() (equality) will return false even when
     * o1 == o2 is true (sameness); this behaviour is intentional.
     */
    @Suppress("EqualsOrHashCode")
    class ObjectIsSameButNotEqual {
        override fun equals(other: Any?): Boolean {
            return false
        }
    }

    /**
     * Special class similar to boxed long where 2 boxed instances
     * of the same value will return true for equals() but will return
     * false for == operator. Long cannot be used since the compiler
     * will return true when the == operator compares 2 different instances
     * of the same long value (e.g. new Long(2) == new Long(3)).
     */
    @Suppress("EqualsOrHashCode")
    class ObjectIsEqualButIsNotSame(var mValue: Long) {
        override fun equals(other: Any?): Boolean {
            return (other is ObjectIsEqualButIsNotSame && other.mValue == mValue)
        }
    }
}

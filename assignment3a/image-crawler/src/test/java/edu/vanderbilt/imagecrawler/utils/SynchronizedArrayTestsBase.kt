package edu.vanderbilt.imagecrawler.utils

import admin.AssignmentTests
import admin.getField
import admin.setField
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.verify
import kotlinx.coroutines.*
import org.junit.Before
import java.util.*
import java.util.concurrent.CountDownLatch
import java.util.function.Consumer
import java.util.function.UnaryOperator
import java.util.stream.Stream
import kotlin.test.*

/**
 * All tests in this class should only test for synchronize calls AFTER testing
 * a method for results. This ordering prevents test from hanging.
 */
@InternalCoroutinesApi
open class SynchronizedArrayTestsBase : AssignmentTests() {
    @MockK
    lateinit var mockArray: Array<Int>

    @MockK
    lateinit var mockDataArray: Array<Int>

    @MockK
    lateinit var mockCollection: Collection<Int>

    @MockK
    lateinit var mockIterator: MutableIterator<Int>

    @MockK
    lateinit var mockUnaryOperator: UnaryOperator<Int>

    @MockK
    lateinit var mockConsumer: Consumer<Int>

    @MockK
    lateinit var mockStream: Stream<Int>

    @MockK
    lateinit var mockSpliterator: Spliterator<Int>

    private val array = SynchronizedArray<Int>()

    private fun testSynchronized(name: String, block: () -> Unit) {
        val start = CountDownLatch(1)
        val stop = CountDownLatch(1)

        // Coroutine that holds a synchronized lock on the test array.
        val blocker = GlobalScope.launch {
            synchronized(array) {
                start.countDown()
                stop.await()
            }
        }

        // Coroutine that runs the passed block of code.
        val runner = GlobalScope.async(start = CoroutineStart.LAZY) {
            block()
        }

        // Blocking main thread coroutine.
        runBlocking {
            start.await()

            try {
                withTimeoutOrNull(10) {
                    runner.await()
                    fail("Method $name is not synchronized")
                }
            } finally {
                stop.countDown()
                blocker.join()
            }

            runner.await()
        }
    }

    @Before
    open fun before() {
        array.setField("", mockArray, Array::class.java)
    }

    open fun testConstructorWithArray() {
        val input = UnsynchronizedArray<Int>()
        val sut = SynchronizedArray(input)
        val wrappedArray = sut.getField<Array<Int>>("")
        assertSame(input, wrappedArray)
    }

    open fun testConstructorWithCollection() {
        val input = listOf(1, 2, 3)
        val sut = SynchronizedArray(input)
        val wrappedArray = sut.getField<Array<Int>>("")
        assertEquals(input, wrappedArray.toList())
    }

    open fun isEmpty() {
        every { mockArray.isEmpty } returns false
        assertFalse(array.isEmpty)
        verify(exactly = 1) { mockArray.isEmpty() }

        testSynchronized(Exception().stackTrace[0].methodName) {
            array.isEmpty
        }
    }

    open fun size() {
        every { mockArray.size() } returns -1
        assertEquals(-1, array.size())
        verify(exactly = 1) { mockArray.size() }

        testSynchronized(Exception().stackTrace[0].methodName) {
            array.size()
        }
    }

    open fun indexOf() {
        every { mockArray.indexOf(0) } returns 100
        assertEquals(100, array.indexOf(0))
        verify(exactly = 1) { mockArray.indexOf(0) }

        testSynchronized(Exception().stackTrace[0].methodName) {
            array.indexOf(0)
        }
    }

    open fun addAllCollection() {
        every { mockArray.addAll(mockCollection) } returns true
        assertEquals(true, array.addAll(mockCollection))
        verify(exactly = 1) { mockArray.addAll(mockCollection) }

        testSynchronized(Exception().stackTrace[0].methodName) {
            array.addAll(mockCollection)
        }
    }

    open fun addAllArray() {
        every { mockArray.addAll(mockDataArray) } returns true
        assertEquals(true, array.addAll(mockDataArray))
        verify(exactly = 1) { mockArray.addAll(mockDataArray) }

        testSynchronized(Exception().stackTrace[0].methodName) {
            array.addAll(mockDataArray)
        }
    }

    open fun remove() {
        every { mockArray.remove(1) } returns 1000
        assertEquals(1000, array.remove(1))
        verify(exactly = 1) { mockArray.remove(1) }

        testSynchronized(Exception().stackTrace[0].methodName) {
            array.remove(1)
        }
    }

    open fun get() {
        every { mockArray.get(1) } returns 1000
        assertEquals(1000, array.get(1))
        verify(exactly = 1) { mockArray.get(1) }

        testSynchronized(Exception().stackTrace[0].methodName) {
            array.get(1)
        }
    }

    open fun set() {
        every { mockArray.set(1, 10) } returns 1000
        assertEquals(1000, array.set(1, 10))
        verify(exactly = 1) { mockArray.set(1, 10) }

        testSynchronized(Exception().stackTrace[0].methodName) {
            array.set(1, 10)
        }
    }

    open fun add() {
        every { mockArray.add(1) } returns true
        assertEquals(true, array.add(1))
        verify(exactly = 1) { mockArray.add(1) }

        testSynchronized(Exception().stackTrace[0].methodName) {
            array.add(1)
        }
    }

    open fun toUnsynchronizedArray() {
        assertNotEquals(mockArray, array.toUnsynchronizedArray())

        testSynchronized(Exception().stackTrace[0].methodName) {
            array.toUnsynchronizedArray()
        }
    }

    open fun uncheckedToArray() {
        val data = Array<Any>(1) {}

        every { mockArray.uncheckedToArray() } returns data
        assertEquals(data, array.uncheckedToArray())
        verify(exactly = 1) { mockArray.uncheckedToArray() }

        testSynchronized(Exception().stackTrace[0].methodName) {
            array.uncheckedToArray()
        }
    }

    open fun toArray() {
        val data = Array<Any>(1) {}

        every { mockArray.toArray() } returns data
        assertEquals(data, array.toArray())
        verify(exactly = 1) { mockArray.toArray() }

        testSynchronized(Exception().stackTrace[0].methodName) {
            array.toArray()
        }
    }

    open fun toArrayTypedArray() {
        val data = Array<Any>(1) {}

        every { mockArray.toArray(data) } returns data
        assertEquals(data, array.toArray(data))
        verify(exactly = 1) { mockArray.toArray(data) }

        testSynchronized(Exception().stackTrace[0].methodName) {
            array.toArray(data)
        }
    }

    open fun iterator() {
        every { mockArray.iterator() } returns mockIterator
        assertEquals(mockIterator, array.iterator())
        verify(exactly = 1) { mockArray.iterator() }
    }

    open fun replaceAll() {
        every { mockArray.replaceAll(mockUnaryOperator) } returns Unit
        array.replaceAll(mockUnaryOperator)
        verify(exactly = 1) { mockArray.replaceAll(mockUnaryOperator) }

        testSynchronized(Exception().stackTrace[0].methodName) {
            array.replaceAll(mockUnaryOperator)
        }
    }

    open fun forEach() {
        every { mockArray.forEach(mockConsumer) } returns Unit
        array.forEach(mockConsumer)
        verify(exactly = 1) { mockArray.forEach(mockConsumer) }

        testSynchronized(Exception().stackTrace[0].methodName) {
            array.forEach(mockConsumer)
        }
    }

    open fun parallelStream() {
        every { mockArray.parallelStream() } returns mockStream
        assertEquals(mockStream, array.parallelStream())
        verify(exactly = 1) { mockArray.parallelStream() }
    }

    open fun stream() {
        every { mockArray.stream() } returns mockStream
        assertEquals(mockStream, array.stream())
        verify(exactly = 1) { mockArray.stream() }
    }

    open fun spliterator() {
        every { mockArray.spliterator() } returns mockSpliterator
        assertEquals(mockSpliterator, array.spliterator())
        verify(exactly = 1) { mockArray.spliterator() }
    }
}

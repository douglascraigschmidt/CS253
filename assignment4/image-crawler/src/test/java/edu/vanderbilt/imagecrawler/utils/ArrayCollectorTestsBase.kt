package edu.vanderbilt.imagecrawler.utils

import admin.AssignmentTests
import edu.vanderbilt.imagecrawler.utils.Student.isGraduate
import io.mockk.MockKAnnotations
import io.mockk.impl.annotations.MockK
import io.mockk.impl.annotations.SpyK
import io.mockk.verify
import org.junit.Before
import java.util.function.Function
import java.util.stream.Collector
import kotlin.test.*

open class ArrayCollectorTestsBase : AssignmentTests() {
    @SpyK
    var collector: ArrayCollector<Int> = ArrayCollector()

    @MockK
    lateinit var array: UnsynchronizedArray<Int>

    @MockK
    lateinit var arrayExtra: UnsynchronizedArray<Int>

    @Before
    fun initializeMockKAnnotations() {
        MockKAnnotations.init(this, relaxUnitFun = true)
    }

    open fun supplier() {
        val result = ArrayCollector<Any>().supplier()
        assertNotNull(result)

        if (isGraduate()) {
            assertTrue(result.get() is SynchronizedArray)
        } else {
            assertTrue(result.get() is UnsynchronizedArray)
        }
    }

    open fun accumulator() {
        val result = collector.accumulator()
        assertNotNull(result)
        result.accept(array, 2)
        verify(exactly = 1) { array.add(2) }
    }

    open fun combiner() {
        val result = collector.combiner()

        if (isGraduate()) {
            assertNull(result)
        } else {
            assertNotNull(result)
            result.apply(array, arrayExtra)
            verify(exactly = 1) { array.addAll(arrayExtra) }
        }
    }

    open fun finisher() {
        assertEquals(null, collector.finisher())
    }

    open fun characteristics() {
        val result = collector.characteristics()
        assertNotNull(result)
        assertTrue(result.contains(Collector.Characteristics.UNORDERED))
        assertTrue(result.contains(Collector.Characteristics.IDENTITY_FINISH))
        assertEquals(result.contains(Collector.Characteristics.CONCURRENT), isGraduate())
    }

    open fun toArray() {
        val result = ArrayCollector.toArray<Any>()
        assertNotNull(result)
        assertTrue(result is ArrayCollector)
    }
}

package edu.vanderbilt.imagecrawler.crawlers

import admin.AssignmentTests
import edu.vanderbilt.imagecrawler.transforms.Transform
import edu.vanderbilt.imagecrawler.utils.Image
import io.mockk.*
import io.mockk.impl.annotations.MockK
import io.mockk.impl.annotations.SpyK
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.core.ObservableTransformer
import io.reactivex.rxjava3.functions.Function
import io.reactivex.rxjava3.functions.Predicate
import io.reactivex.rxjava3.schedulers.Schedulers
import org.assertj.core.api.Assertions.assertThat
import org.junit.Before
import org.junit.Test

class RxObservableLocalCrawlerTests : AssignmentTests() {
    @SpyK
    var crawler: RxObservableCrawler = RxObservableCrawler()

    @MockK
    lateinit var mockImage: Image

    private val trampoline = Schedulers.trampoline()

    @Before
    fun before() {
        mockkStatic(Schedulers::class)
        every { Schedulers.io() } returns trampoline
        every { this@RxObservableLocalCrawlerTests.crawler.runLocalTransforms() } returns true
        every { this@RxObservableLocalCrawlerTests.crawler.runRemoteTransforms() } returns false
    }

    @Test
    fun `transformImageAsync uses correct chain calls and lambdas`() {
        val input = mockk<Image>()
        val os = mockk<Observable<Transform>>()
        val io = mockk<Observable<Image>>()
        val ot = mockk<ObservableTransformer<Any, Any>>()
        val mi = mockk<Image>()

        mockkStatic(Observable::class)
        mockkStatic(RxObservableCrawler::class)

        every { Observable.fromIterable<Transform>(any()) } answers { os }
        every { os.subscribeOn(any()) } answers { os }
        every { os.filter(any()) } answers {
            firstArg<Predicate<Transform>>().test(mockk())
            os
        }
        every { os.compose<Image>(any()) } answers { io }
        every { crawler.createNewCacheItem(any<Image>(), any()) } answers { true }
        every { RxObservableCrawler.mapNotNull<Any, Any>(any()) } answers {
            firstArg<Function<Transform, Image>>().apply(mockk())
            ot
        }
        every { crawler.applyTransform(any(), any()) } answers { mi }

        assertThat(crawler.transformImageAsync(input)).isSameAs(io)

        verify {
            Observable.fromIterable<Transform>(any())
            os.subscribeOn(any())
            os.filter(any())
            os.compose<Image>(any())
            crawler.createNewCacheItem(any<Image>(), any())
            RxObservableCrawler.mapNotNull<Any, Any>(any())
            crawler.applyTransform(any(), any())
            crawler.transformImageAsync(any())
            crawler.runRemoteTransforms()
        }

        confirmVerified(os, crawler)
    }
}
package edu.vanderbilt.imagecrawler.crawlers

import admin.AssignmentTests
import edu.vanderbilt.imagecrawler.transforms.Transform
import edu.vanderbilt.imagecrawler.utils.Image
import io.mockk.*
import io.mockk.impl.annotations.MockK
import io.mockk.impl.annotations.SpyK
import io.reactivex.rxjava3.core.Flowable
import io.reactivex.rxjava3.functions.Function
import io.reactivex.rxjava3.functions.Predicate
import io.reactivex.rxjava3.parallel.ParallelFlowable
import io.reactivex.rxjava3.parallel.ParallelTransformer
import io.reactivex.rxjava3.schedulers.Schedulers
import org.assertj.core.api.Assertions.assertThat
import org.junit.Before
import org.junit.Test

class RxFlowableLocalCrawlerTests : AssignmentTests() {
    @SpyK
    var crawler: RxFlowableCrawler = RxFlowableCrawler()

    @MockK
    lateinit var mockImage: Image

    private val trampoline = Schedulers.trampoline()

    @Before
    fun before() {
        mockkStatic(Schedulers::class)
        every { crawler.log(any(), *anyVararg()) } answers { }
        every { Schedulers.io() } returns trampoline
        every { this@RxFlowableLocalCrawlerTests.crawler.runLocalTransforms() } returns true
        every { this@RxFlowableLocalCrawlerTests.crawler.runRemoteTransforms() } returns false
    }

    @Test
    fun `transformImageAsync (local) uses correct chain calls and lambdas`() {
        val input = mockk<Image>()
        val ft = mockk<Flowable<Transform>>()
        val fi = mockk<Flowable<Image>>()
        val pf = mockk<ParallelFlowable<Transform>>()
        val i = mockk<Image>()
        val t = mockk<Transform>()
        val pti = mockk<ParallelTransformer<Transform, Image>>()
        val pfi = mockk<ParallelFlowable<Image>>()
        mockkStatic(Flowable::class)
        mockkStatic(RxFlowableCrawler::class)

        every { Flowable.fromIterable<Transform>(any()) } answers { ft }
        every { ft.parallel() } answers { pf }
        every { pf.runOn(any()) } answers { pf }
        every {
            crawler.createNewCacheItem(any(), any<Transform>())
        } returnsMany listOf(true, false)
        every { pf.filter(any()) } answers {
            assertThat(firstArg<Predicate<Transform>>().test(mockk())).isTrue
            assertThat(firstArg<Predicate<Transform>>().test(mockk())).isFalse
            firstArg<Predicate<Transform>>().test(mockk())
            pf
        }
        every { crawler.applyTransform(any(), any()) } answers { i }
        every { pf.compose(any<ParallelTransformer<Transform, Image>>()) } answers {
            firstArg<ParallelTransformer<Transform, Image>>().apply(mockk())
            pfi
        }
        every { RxFlowableCrawler.mapNotNull(any<Function<Transform, Image>>()) } answers {
            firstArg<Function<Transform, Image>>().apply(t)
            pti
        }
        every { pfi.sequential() } answers { fi }
        every { crawler.applyTransform(any(), any()) } answers { i }

        assertThat(crawler.transformImageAsync(input)).isSameAs(fi)

        verify {
            Flowable.fromIterable<Transform>(any())
            crawler.createNewCacheItem(any(), any<Transform>())
            ft.parallel()
            pf.runOn(any())
            pf.filter(any())
            crawler.transformImageAsync(input)
            crawler.applyTransform(any(), any())
            pf.compose(any<ParallelTransformer<Transform, Image>>())
            RxFlowableCrawler.mapNotNull(any<Function<Transform, Image>>())
            pfi.sequential()
            crawler.applyTransform(any(), any())
            crawler.runRemoteTransforms()
            pti.apply(any())
        }

        confirmVerified(crawler, ft, fi, pf, i, t, pti, pfi)
    }
}
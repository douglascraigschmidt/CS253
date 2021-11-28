package edu.vanderbilt.imagecrawler.crawlers

import admin.AssignmentTests
import admin.injectInto
import edu.vanderbilt.imagecrawler.transforms.Transform
import edu.vanderbilt.imagecrawler.utils.Image
import io.mockk.*
import io.mockk.impl.annotations.MockK
import io.mockk.impl.annotations.SpyK
import org.assertj.core.api.Assertions.assertThat
import org.junit.Before
import org.junit.Test
import reactor.core.publisher.Flux
import reactor.core.scheduler.Schedulers

class ReactorLocalCrawlerTests : AssignmentTests() {

    @SpyK
    var spyCrawler: ReactorCrawler = ReactorCrawler()

    @MockK
    lateinit var mockImageFlux: Flux<Image>

    @MockK
    lateinit var mockTransformFlux: Flux<Transform>

    @MockK
    lateinit var mockImage: Image

    @MockK
    lateinit var mockTransforms: List<Transform>

    private val immediate = Schedulers.immediate()

    @Before
    fun before() {
        mockkStatic(Schedulers::class)
        every { Schedulers.parallel() } returns immediate
        every { Schedulers.immediate() } throws Exception("Schedulers.immediate() should not be called.")
        every { Schedulers.newParallel(any()) } throws Exception("Schedulers.newParallel() should not be called.")
        every {
            Schedulers.newParallel(
                any(),
                any<Int>()
            )
        } throws Exception("Schedulers.newParallel() should not be called.")
        every { Schedulers.single() } throws Exception("Schedulers.single() should not be called.")
    }

    @Test
    fun `transformImageLocally uses the expected Project Reactor outer chained method calls`() {
        mockkStatic(Flux::class)
        every { Flux.fromIterable<Transform>(any()) } returns mockTransformFlux
        every { mockTransformFlux.mapNotNull<Image>(any()) } returns mockImageFlux
        every { mockTransformFlux.filter(any()) } returns mockTransformFlux
        every { mockTransformFlux.subscribeOn(any()) } returns mockTransformFlux

        // SUT
        spyCrawler.mTransforms = mockTransforms
        assertThat(spyCrawler.transformImageLocally(mockImage)).isSameAs(mockImageFlux)

        verify(exactly = 1) {
            mockTransformFlux.mapNotNull<Image>(any())
            mockTransformFlux.filter(any())
            Flux.fromIterable<Transform>(any())
            spyCrawler.transformImageLocally(any())
            mockTransformFlux.subscribeOn(any())
        }
        confirmVerified(spyCrawler, mockTransformFlux, mockImageFlux)
    }

    @Test
    fun `transformImageAsync uses the expected Project Reactor inner chained method calls`() {
        val mockTransform = mockk<Transform>()
        listOf(mockTransform, mockTransform, mockTransform).apply {
            injectInto(spyCrawler)
        }
        val transformList = listOf(null, mockImage, null)
        val total = spyCrawler.mTransforms.count()
        val expected = transformList.count { image -> image != null }

        every { spyCrawler.applyTransform(any(), any()) } returnsMany transformList
        every { spyCrawler.createNewCacheItem(any(), any<Transform>()) } returns true

        // SUT
        assertThat(spyCrawler.transformImageLocally(mockImage).count().block() ?: null)
            .isEqualTo(expected.toLong())

        verify(exactly = total) {
            spyCrawler.applyTransform(any(), any())
            spyCrawler.createNewCacheItem(any(), any<Transform>())
        }
        verify(exactly = 1) { spyCrawler.transformImageLocally(any()) }
        confirmVerified(spyCrawler, mockTransform)
    }
}

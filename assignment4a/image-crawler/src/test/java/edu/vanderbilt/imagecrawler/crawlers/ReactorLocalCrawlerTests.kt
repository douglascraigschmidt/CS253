package edu.vanderbilt.imagecrawler.crawlers

import admin.AssignmentTests
import admin.injectInto
import edu.vanderbilt.imagecrawler.transforms.Transform
import edu.vanderbilt.imagecrawler.utils.Image
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.impl.annotations.SpyK
import io.mockk.mockk
import io.mockk.mockkStatic
import io.mockk.verify
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
    }

    @Test
    fun `transformImageLocally uses the expected Project Reactor outer chained method calls`() {
        mockkStatic(Flux::class)
        every { Flux.fromIterable<Transform>(any()) } returns mockTransformFlux
        every { mockTransformFlux.subscribeOn(any()) } returns mockTransformFlux
        every { mockTransformFlux.filter(any()) } returns mockTransformFlux
        every { mockTransformFlux.mapNotNull<Image>(any()) } returns mockImageFlux

        // SUT
        spyCrawler.mTransforms = mockTransforms
        spyCrawler.transformImageLocally(mockImage)

        every { Flux.fromIterable<Transform>(any()) }
        every { mockTransformFlux.subscribeOn(any()) }
        every { mockTransformFlux.filter(any()) }
        every { mockTransformFlux.mapNotNull<Image>(any()) }
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

        every { spyCrawler.createNewCacheItem(any(), any<Transform>()) } returns true
        every { spyCrawler.applyTransform(any(), any()) } returnsMany transformList

        // SUT
        assertThat(spyCrawler.transformImageLocally(mockImage).count().block() ?: null)
            .isEqualTo(expected.toLong())

        verify(exactly = total) { spyCrawler.createNewCacheItem(any(), any<Transform>()) }
        verify(exactly = total) { spyCrawler.applyTransform(any(), any()) }
    }
}

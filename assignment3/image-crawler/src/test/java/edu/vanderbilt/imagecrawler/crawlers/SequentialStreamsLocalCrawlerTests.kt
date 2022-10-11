package edu.vanderbilt.imagecrawler.crawlers

import admin.AssignmentTests
import edu.vanderbilt.imagecrawler.transforms.Transform
import edu.vanderbilt.imagecrawler.utils.Image
import io.mockk.MockKAnnotations
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.impl.annotations.SpyK
import io.mockk.mockk
import io.mockk.verify
import org.assertj.core.api.Assertions.assertThat
import org.junit.Before
import org.junit.Test
import java.util.stream.Stream

class SequentialStreamsLocalCrawlerTests : AssignmentTests() {
    @SpyK
    var mockCrawler = SequentialStreamsCrawler()

    @MockK
    lateinit var mockImageStream: Stream<Image>

    @MockK
    lateinit var mockTransforms: List<Transform>

    @MockK
    lateinit var mockTransformStream: Stream<Transform>

    @MockK
    lateinit var mockTransform: Transform

    @MockK
    lateinit var mockImage: Image

    private val expected = -99

    @Before
    fun before() {
        MockKAnnotations.init(this, relaxUnitFun = true)
        every { mockCrawler.log(any(), *anyVararg()) } answers { }
        every { mockCrawler.runLocalTransforms() } returns true
        every { mockCrawler.runRemoteTransforms() } returns false
    }

    @Test
    fun `transformImageLocally() uses expected chained method calls`() {
        val mockImage = mockk<Image>()

        mockCrawler.mTransforms = mockTransforms

        every { mockTransforms.stream() } returns mockTransformStream
        every { mockTransformStream.map<Image>(any()) } returns mockImageStream
        every { mockImageStream.filter(any()) } returns mockImageStream
        every { mockImageStream.count() } returns expected.toLong()
        every { mockTransformStream.filter(any()) } returns mockTransformStream

        assertThat(mockCrawler.transformImageLocally(mockImage)).isEqualTo(expected)

        verify(exactly = 1) {
            mockImageStream.filter(any())
            mockImageStream.count()
            mockTransforms.stream()
            mockTransformStream.filter(any())
            mockTransformStream.map<Image>(any())
        }
    }

    @Test
    fun `transformImageLocally() uses expected lambdas within chained method calls`() {
        mockCrawler.mTransforms = listOf(mockTransform, mockTransform, mockTransform)
        val count = mockCrawler.mTransforms.size

        every { mockCrawler.createNewCacheItem(any(), mockTransform) } returns true
        every { mockCrawler.applyTransform(mockTransform, mockImage) } returns mockImage

        assertThat(mockCrawler.transformImageLocally(mockImage)).isEqualTo(count)

        verify(exactly = count) {
            mockCrawler.createNewCacheItem(any(), mockTransform)
            mockCrawler.applyTransform(mockTransform, mockImage)
        }
    }

    @Test
    fun `transformImageLocally() ignores previously cached transformed images`() {
        mockCrawler.mTransforms = listOf(mockTransform, mockTransform, mockTransform)
        val count = mockCrawler.mTransforms.size

        every { mockCrawler.createNewCacheItem(any(), mockTransform) } returnsMany listOf(true, false, true)
        every { mockCrawler.applyTransform(mockTransform, mockImage) } returnsMany listOf(mockImage, null)

        assertThat(mockCrawler.transformImageLocally(mockImage)).isEqualTo(count - 2)

        verify(exactly = count) { mockCrawler.createNewCacheItem(any(), mockTransform) }
        verify(exactly = count - 1) { mockCrawler.applyTransform(mockTransform, mockImage) }
    }
}
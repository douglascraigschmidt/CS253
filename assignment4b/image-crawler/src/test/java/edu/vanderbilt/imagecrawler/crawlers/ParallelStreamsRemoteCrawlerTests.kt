package edu.vanderbilt.imagecrawler.crawlers

import admin.AssignmentTests
import edu.vanderbilt.imagecrawler.utils.Image
import edu.vanderbilt.imagecrawler.web.RemoteDataSource
import edu.vanderbilt.imagecrawler.utils.TransformedImage
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.impl.annotations.SpyK
import io.mockk.mockk
import io.mockk.spyk
import io.mockk.verify
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test
import java.util.stream.Stream

class ParallelStreamsRemoteCrawlerTests : AssignmentTests() {

    @SpyK
    var spyCrawler: ParallelStreamsCrawler =
        ParallelStreamsCrawler()

    @MockK
    lateinit var mockImage: Image

    @Test
    fun `transformImageRemotely uses the expected chained method calls`() {
        val expected = -99
        val mockRemoteDataSource = mockk<RemoteDataSource>()
        val mockTransformedImageStream = mockk<Stream<TransformedImage>>()
        val mockTransformedImageList = mockk<List<TransformedImage>>()
        val mockTransformNames = mockk<List<String>>()
        val mockImageStream = mockk<Stream<Image>>()
        every { mockImageStream.count() } answers { expected.toLong() }
        every { spyCrawler.remoteDataSource } answers { mockRemoteDataSource }
        every { spyCrawler.transformNames } answers { mockTransformNames }
        every { mockTransformedImageList.parallelStream() } answers { mockTransformedImageStream }
        every { mockTransformedImageStream.map<Image>(any()) } answers { mockImageStream }
        every { mockImageStream.filter(any()) } answers { mockImageStream }
        every {
            mockRemoteDataSource.applyTransforms(
                any(),
                mockImage,
                mockTransformNames,
                true
            )
        } answers { mockTransformedImageList }

        assertThat(spyCrawler.transformImageRemotely(mockImage)).isEqualTo(expected)

        verify(exactly = 1) {
            mockTransformedImageStream.map<Image>(any())
            mockRemoteDataSource.applyTransforms(any(), mockImage, mockTransformNames, true)
            spyCrawler.remoteDataSource
            mockTransformedImageList.parallelStream()
            mockImageStream.count()
            mockImageStream.filter(any())
            spyCrawler.transformNames
        }
    }

    @Test
    fun `transformImageRemotely uses the expected lambdas`() {
        val expected = 2
        val mockTransformNames = mockk<List<String>>()
        val spyRemoteDataSource = spyk(RemoteDataSource("http://www.mock.com"))
        val mockTransformedImage = mockk<TransformedImage>()
        val spyTransformedImageList = spyk(listOf(mockTransformedImage, mockTransformedImage))
        every { spyCrawler.remoteDataSource } answers { spyRemoteDataSource }
        every { spyCrawler.transformNames } answers { mockTransformNames }
        every { spyCrawler.createImage(any(), any()) } answers { mockImage }
        every {
            spyRemoteDataSource.applyTransforms(
                any(),
                mockImage,
                mockTransformNames,
                true
            )
        } answers { spyTransformedImageList }
        assertThat(spyCrawler.transformImageRemotely(mockImage)).isEqualTo(expected)
        verify(exactly = 1) {
            spyCrawler.remoteDataSource
            spyRemoteDataSource.applyTransforms(any(), mockImage, mockTransformNames, true)
            spyCrawler.transformNames
        }
        verify(exactly = expected) { spyCrawler.createImage(any(), any()) }
    }
}
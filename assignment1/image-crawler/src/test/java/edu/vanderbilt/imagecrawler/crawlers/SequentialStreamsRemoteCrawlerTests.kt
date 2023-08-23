package edu.vanderbilt.imagecrawler.crawlers

import admin.AssignmentTests
import edu.vanderbilt.imagecrawler.utils.Image
import edu.vanderbilt.imagecrawler.utils.TransformedImage
import edu.vanderbilt.imagecrawler.web.RemoteDataSource
import io.mockk.*
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test
import java.util.function.Function
import java.util.function.Predicate
import java.util.stream.Stream

class SequentialStreamsRemoteCrawlerTests : AssignmentTests() {

    @Test
    fun `transformImageRemotely uses expected chained method calls and lambdas `() {
        val crawler = spyk<SequentialStreamsCrawler>()
        val si = mockk<Stream<Image>>()
        val i = mockk<Image>()
        val tis = mockk<Stream<TransformedImage>>()
        val mt = mockk<MutableList<String>>()
        val lti = mockk<MutableList<TransformedImage>>()
        val rd = mockk<RemoteDataSource>()
        every { tis.map<Image>(any()) } answers {
            firstArg<Function<TransformedImage, Image>>().apply(mockk())
            si
        }
        every { crawler.createImage(any(), any()) } returns i
        every { rd.applyTransforms(any(), any(), any(), any()) } returns lti
        every { crawler.remoteDataSource } returns rd
        every { crawler.transformNames } answers { mt }
        every { crawler.remoteDataSource } returns rd
        every { si.filter(any()) } answers {
            firstArg<Predicate<Image>>().test(mockk())
            si
        }
        every { lti.stream() } returns tis
        assertThat(crawler.transformImageRemotely(i)).isEqualTo(si)
        verify {
            tis.map<Image>(any())
            crawler.createImage(any(), any())
            crawler.transformImageRemotely(i)
            rd.applyTransforms(any(), any(), any(), any())
            crawler.remoteDataSource
            crawler.transformNames
            si.filter(any())
            lti.stream()
        }
        confirmVerified(crawler, tis, rd, si, lti, i)
    }
}
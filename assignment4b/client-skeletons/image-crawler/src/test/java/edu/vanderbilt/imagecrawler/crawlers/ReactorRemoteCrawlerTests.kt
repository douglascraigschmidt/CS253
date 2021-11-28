package edu.vanderbilt.imagecrawler.crawlers

import admin.AssignmentTests
import edu.vanderbilt.imagecrawler.utils.Image
import edu.vanderbilt.imagecrawler.web.RemoteDataSource
import edu.vanderbilt.imagecrawler.web.TransformedImage
import io.mockk.*
import io.mockk.impl.annotations.MockK
import io.mockk.impl.annotations.SpyK
import org.assertj.core.api.Assertions.assertThat
import org.junit.Before
import org.junit.Test
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import reactor.core.scheduler.Schedulers

class ReactorRemoteCrawlerTests : AssignmentTests() {

    @SpyK
    var spyCrawler: ReactorCrawler = ReactorCrawler()

    @MockK
    lateinit var mockImage: Image

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
    fun `transformImageRemotely uses the expected Project Reactor inner chained method calls`() {
        val api: RemoteDataSource.TransformApi = mockk()
        val remoteDataSource: RemoteDataSource = mockk()
        val fluxTransformedImage: Flux<TransformedImage> = mockk()
        val transformNames: List<String> = mockk()
        val fluxImage: Flux<Image> = mockk()
        val monoList: Mono<List<TransformedImage>> = mockk()

        every { api.applyReactorTransforms(any(), any(), true) } returns monoList
        every { spyCrawler.remoteDataSource } returns remoteDataSource
        every { fluxTransformedImage.map<Image>(any()) } returns fluxImage
        every { spyCrawler.transformNames } returns transformNames
        every { monoList.flatMapMany<TransformedImage>(any()) } returns fluxTransformedImage
        every { remoteDataSource.api } returns api

        assertThat(spyCrawler.transformImageRemotely(mockImage)).isSameAs(fluxImage)

        verify(exactly = 1) {
            fluxTransformedImage.map<Image>(any())
            spyCrawler.transformNames
            api.applyReactorTransforms(any(), any(), true)
            remoteDataSource.api
            remoteDataSource.buildMultipartBodyPart(any(), any())
            monoList.flatMapMany<TransformedImage>(any())
            spyCrawler.transformImageRemotely(any())
        }
        verify(exactly = 2) { spyCrawler.remoteDataSource }
        confirmVerified(api, remoteDataSource, fluxImage, fluxTransformedImage, transformNames, monoList)
    }
}
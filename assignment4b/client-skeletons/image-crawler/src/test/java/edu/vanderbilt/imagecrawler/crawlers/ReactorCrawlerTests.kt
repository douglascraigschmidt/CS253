package edu.vanderbilt.imagecrawler.crawlers

import admin.AssignmentTests
import admin.injectInto
import edu.vanderbilt.imagecrawler.utils.ConcurrentHashSet
import edu.vanderbilt.imagecrawler.utils.Crawler
import edu.vanderbilt.imagecrawler.utils.Image
import edu.vanderbilt.imagecrawler.utils.WebPageCrawler
import io.mockk.*
import io.mockk.impl.annotations.MockK
import io.mockk.impl.annotations.SpyK
import org.assertj.core.api.Assertions.assertThat
import org.junit.After
import org.junit.Before
import org.junit.Test
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import reactor.core.scheduler.Schedulers
import java.net.URL
import java.util.*
import java.util.function.Function
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

open class ReactorCrawlerTests : AssignmentTests() {
    @SpyK
    var spyCrawler: ReactorCrawler = ReactorCrawler()

    @MockK
    lateinit var mockImageFlux: Flux<Image>

    @MockK
    lateinit var mockImageMono: Mono<Image>

    @MockK
    lateinit var mockImage: Image

    @MockK
    lateinit var mockLongMono: Mono<Long>

    @MockK
    lateinit var mockStringArray: List<String>

    @MockK
    lateinit var mockCrawlerPageFlux: Flux<Crawler.Page>

    @MockK
    lateinit var mockUrlFlux: Flux<URL>

    @MockK
    lateinit var mockUrlMono: Mono<URL>

    @MockK
    lateinit var mockPage: Crawler.Page

    @MockK
    lateinit var mockStringFlux: Flux<String>

    private val immediate = Schedulers.immediate()

    @Before
    open fun before() {
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

    @After
    fun after() {
        clearAllMocks()
    }

    @Test
    fun performCrawl() {
        every { mockImageFlux.count() } returns mockLongMono
        every { mockLongMono.block() } returns 1
        every { mockLongMono.onErrorReturn(any()) } returns mockLongMono
        every { spyCrawler.crawlPageAsync(any(), any()) } returns mockImageFlux

        // SUT
        spyCrawler.performCrawl("test", 3)

        verify(exactly = 1) {
            mockImageFlux.count()
            spyCrawler.crawlPageAsync(any(), 3)
            mockLongMono.block()
            mockLongMono.onErrorReturn(0L)
            spyCrawler.performCrawl(any(), any())
        }
        confirmVerified(spyCrawler, mockImageFlux, mockLongMono)
    }

    @Test
    fun `crawlPageAsync uses the expected Project Reactor outer chained method calls`() {
        mockkStatic(Flux::class)
        val mockWebPageCrawler = mockk<WebPageCrawler>()
        mockWebPageCrawler.injectInto(spyCrawler)
        every { mockCrawlerPageFlux.flatMap<Image>(any()) } returns mockImageFlux
        every { mockStringFlux.map<Crawler.Page>(any()) } returns mockCrawlerPageFlux
        every { mockStringFlux.filter(any()) } returns mockStringFlux
        every { Flux.just<String>(any()) } returns mockStringFlux

        // SUT
        spyCrawler.crawlPageAsync("mock", 3).toStream().forEach {}

        verify(exactly = 1) {
            mockCrawlerPageFlux.flatMap<Image>(any())
            mockStringFlux.filter(any())
            Flux.just<String>(any())
            mockStringFlux.map<Crawler.Page>(any())
            mockImageFlux.toStream()
            spyCrawler.crawlPageAsync(any(), any())
        }
        confirmVerified(spyCrawler, mockWebPageCrawler, mockCrawlerPageFlux, mockStringFlux, mockImageFlux)
    }

    @Test
    fun `crawlPageAsync uses the expected Project Reactor inner lambda calls`() {
        val mockWebPageCrawler = mockk<WebPageCrawler>()
        spyCrawler.mMaxDepth = Int.MAX_VALUE
        mockWebPageCrawler.injectInto(spyCrawler)
        val mockHashSet = mockk<ConcurrentHashSet<String>>()
        mockHashSet.injectInto(spyCrawler)
        every { mockHashSet.putIfAbsent(any()) } returns true
        every { mockWebPageCrawler.getPage(any()) } returns mockPage

        // SUT
        spyCrawler.crawlPageAsync("mock", 3).toStream().forEach {}

        verify(exactly = 1) {
            mockHashSet.putIfAbsent(any())
            mockWebPageCrawler.getPage(any())
            spyCrawler.crawlPageAsync(any(), 3)
            spyCrawler.imagesOnPageAndPageLinksAsync(any(), 3)
            spyCrawler.imagesOnPageAsync(mockPage)
            spyCrawler.imagesOnPageLinksAsync(mockPage, 3)
            mockPage.getPageElementsAsUrls(*anyVararg())
            mockPage.getPageElementsAsStrings(*anyVararg())
        }
        confirmVerified(mockWebPageCrawler, spyCrawler, mockHashSet, mockPage)
    }

    @Test
    fun `imagesOnPageAndPageLinksAsync uses the expected Project Reactor outer chained method calls`() {
        mockkStatic(Flux::class)
        mockkStatic(Mono::class)

        every { mockImageFlux.mergeWith(mockImageFlux) } returns mockImageFlux
        every { spyCrawler.imagesOnPageLinksAsync(any(), any()) } returns mockImageFlux
        every { spyCrawler.imagesOnPageAsync(any()) } returns mockImageFlux
        every { mockImageFlux.subscribeOn(any()) } returns mockImageFlux

        // SUT
        assertNotNull(spyCrawler.imagesOnPageAndPageLinksAsync(mockPage, 0))

        verify(exactly = 1) {
            mockImageFlux.mergeWith(mockImageFlux)
            spyCrawler.imagesOnPageAsync(mockPage)
            mockImageFlux.subscribeOn(immediate)
            spyCrawler.imagesOnPageLinksAsync(mockPage, 0)
            spyCrawler.imagesOnPageAndPageLinksAsync(mockPage, 0)
        }
        verify(exactly = 0) {
            Flux.from<Any>(any())
            Mono.fromCallable<Any>(any())
        }
        confirmVerified(mockImageFlux, spyCrawler)
    }

    @Test
    fun `imagesOnPageAndPageLinksAsync uses the expected Project Reactor inner chained method calls`() {
        every { spyCrawler.imagesOnPageAsync(any()) } returns Flux.just(mockImage)
        every { spyCrawler.imagesOnPageLinksAsync(any(), any()) } returns Flux.just(mockImage)

        // SUT
        assertNotNull(spyCrawler.imagesOnPageAndPageLinksAsync(mockPage, 3))

        verify(exactly = 1) {
            spyCrawler.imagesOnPageAsync(mockPage)
            spyCrawler.imagesOnPageLinksAsync(mockPage, 3)
            spyCrawler.imagesOnPageAndPageLinksAsync(mockPage, 3)
        }
        confirmVerified(spyCrawler)
    }

    @Test
    fun `imagesOnPageLinksAsync uses the expected Project Reactor outer chained method calls`() {
        mockkStatic(Flux::class)
        val spyImageFlux = spyk<Flux<Image>>()
        every { mockStringFlux.flatMap<Image>(any()) } returns spyImageFlux
        every { Flux.fromIterable<String>(any()) } returns mockStringFlux
        every { mockPage.getPageElementsAsStrings(Crawler.Type.PAGE) } returns mockStringArray

        // SUT
        assertNotNull(spyCrawler.imagesOnPageLinksAsync(mockPage, 3))

        verify(exactly = 1) {
            Flux.fromIterable<String>(any())
            mockStringFlux.flatMap<Image>(any())
            mockPage.getPageElementsAsStrings(Crawler.Type.PAGE)
            spyCrawler.imagesOnPageLinksAsync(any(), any())
        }
        verify(exactly = 0) { spyImageFlux.subscribeOn(any()) }
        confirmVerified(spyImageFlux, mockStringFlux, mockPage, mockStringArray)
    }

    @Test
    fun `imagesOnPageLinksAsync uses the expected Project Reactor inner chained method calls`() {
        every {
            mockPage.getPageElementsAsStrings(any())
        } returns listOf("a", "b", "c")

        val o = mockk<Flux<Image>>()

        every {
            o.subscribeOn(any())
        } returns Flux.just(mockImage, mockImage, mockImage)

        every { spyCrawler.crawlPageAsync(any(), any()) } returns o

        // SUT
        assertThat(
            spyCrawler
                .imagesOnPageLinksAsync(mockPage, 3)
                .collectList()
                .block()
        ).isNotNull

        verify(exactly = 1) {
            mockPage.getPageElementsAsStrings(any())
            spyCrawler.imagesOnPageLinksAsync(any(), any())
        }
        verify(exactly = 3) {
            spyCrawler.crawlPageAsync(any(), 4)
            o.subscribeOn(any())
        }
        confirmVerified(o, mockPage, mockImageFlux, spyCrawler, mockImage)
    }

    @Test
    fun `imagesOnPageAsync uses the expected Project Reactor outer chained method calls`() {
        mockkStatic(Flux::class)
        every { mockImageFlux.flatMap<Image>(any()) } answers {
            val arg = arg<Function<Image, Flux<Image>>>(0)
            arg.apply(mockImage)
            mockImageFlux
        }
        every { mockUrlFlux.flatMap<Image>(any()) } answers {
            val arg = arg<Function<URL, Mono<Image>>>(0)
            arg.apply(URL("http://www.mock.url"))
            mockImageFlux
        }
        every { Flux.fromIterable<URL>(any()) } returns mockUrlFlux
        every { spyCrawler.downloadImageAsync(any()) } returns mockImageMono
        every { spyCrawler.transformImageAsync(any()) } returns mockImageFlux
        every { mockImageMono.subscribeOn(any()) } throws Exception("subscribeOn() should not be called")
        every { mockImageFlux.subscribeOn(any()) } throws Exception("subscribeOn() should not be called")

        // SUT
        assertThat(spyCrawler.imagesOnPageAsync(mockPage)).isSameAs(mockImageFlux)

        verify(exactly = 1) {
            mockUrlFlux.flatMap<Image>(any())
            mockImageFlux.flatMap<Image>(any())
            Flux.fromIterable<URL>(any())
            spyCrawler.downloadImageAsync(any())
            spyCrawler.transformImageAsync(any())
            spyCrawler.imagesOnPageAsync(any())
            mockPage.getPageElementsAsUrls(*anyVararg())
        }
        confirmVerified(mockImageFlux, mockUrlFlux, mockImageMono, mockImageFlux, spyCrawler, mockPage)
    }

    @Test
    fun `imagesOnPageAsync must filter nulls`() {
        mockkStatic(Optional::class)

        val url1 = URL("http://www.dummy.com/url-1")
        val url2 = URL("http://www.dummy.com/url-2")
        val url3 = URL("http://www.dummy.com/url-3")
        val urls = listOf(url1, url2, url3)
        every { mockPage.getPageElementsAsUrls(any()) } returns urls
        every { spyCrawler.getOrDownloadImage(url1) } returns mockImage
        every { spyCrawler.getOrDownloadImage(url2) } returns null
        every { spyCrawler.getOrDownloadImage(url3) } returns mockImage
        every { spyCrawler.transformImageAsync(any()) } answers {
            Flux.just(it.invocation.args[0] as Image)
        }

        // SUT
        assertEquals(2, spyCrawler.imagesOnPageAsync(mockPage).toStream().count())

        verify(exactly = 3) {
            spyCrawler.getOrDownloadImage(any())
            spyCrawler.downloadImageAsync(any())
        }
        verify(exactly = 2) { spyCrawler.transformImageAsync(any()) }
        verify(exactly = 1) {
            spyCrawler.imagesOnPageAsync(any())
            mockPage.getPageElementsAsUrls(any())
        }

        confirmVerified(spyCrawler, mockPage)
    }

    @Test
    fun `downloadImageAsync uses the expected Project Reactor operators`() {
        mockkStatic(Mono::class)

        val url = URL("http://www.dummy.url-1")
        every {
            spyCrawler.getOrDownloadImage(any())
        } throws IllegalStateException(
            "getOrDownloadImage() is called from within " +
                    "the wrong operator method."
        )

        every { mockUrlMono.mapNotNull<Image>(any()) } answers { mockImageMono }
        every { mockUrlMono.subscribeOn(any()) } answers { mockUrlMono }
        every { Mono.fromCallable<URL>(any()) } answers { mockUrlMono }

        // SUT
        assertThat(spyCrawler.downloadImageAsync(url)).isSameAs(mockImageMono)

        verify(exactly = 1) {
            mockUrlMono.mapNotNull<Image>(any())
            Mono.fromCallable<URL>(any())
            mockUrlMono.subscribeOn(any())
        }

        confirmVerified(mockUrlMono)
    }
}
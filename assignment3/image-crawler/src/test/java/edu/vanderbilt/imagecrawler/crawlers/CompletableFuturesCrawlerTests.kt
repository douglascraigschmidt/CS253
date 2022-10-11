package edu.vanderbilt.imagecrawler.crawlers

import admin.AssignmentTests
import edu.vanderbilt.imagecrawler.transforms.Transform
import edu.vanderbilt.imagecrawler.utils.*
import io.mockk.*
import io.mockk.impl.annotations.MockK
import io.mockk.impl.annotations.SpyK
import org.assertj.core.api.Assertions.assertThat
import org.junit.Before
import org.junit.Test
import java.net.URL
import java.util.*
import java.util.concurrent.CompletableFuture
import java.util.concurrent.ConcurrentHashMap
import java.util.function.Function
import java.util.function.Predicate
import java.util.stream.Collector
import java.util.stream.IntStream
import java.util.stream.Stream

class CompletableFuturesCrawlerTests : AssignmentTests() {
    @SpyK
    var mockCrawler = CompletableFuturesCrawler()

    @MockK
    lateinit var mockWebPageCrawler: WebPageCrawler

    @MockK
    lateinit var mockIntFuture: CompletableFuture<Int>

    @MockK
    lateinit var mockUniqueUris: ConcurrentHashMap.KeySetView<String, Boolean>

    @MockK
    lateinit var mockPage: Crawler.Page

    @Before
    fun before() {
        MockKAnnotations.init(this, relaxUnitFun = true)
        every { mockCrawler.log(any(), *anyVararg()) } answers { }
        every { mockCrawler.runLocalTransforms() } returns true
        every { mockCrawler.runRemoteTransforms() } returns false
    }

    @Test
    fun `crawlPageAsync() uses expected chained method calls`() {
        mockkStatic(Stream::class)
        val uri = "mock"
        val mockIntStream = mockk<Stream<CompletableFuture<Int>>>()
        val mockUriStream = mockk<Stream<String>>()
        val mockOptional = mockk<Optional<CompletableFuture<Int>>>()
        val expected = mockk<CompletableFuture<Int>>()

        every { Stream.of(uri) } returns mockUriStream
        every { mockUriStream.filter(any()) } returns mockUriStream
        every { mockUriStream.map<CompletableFuture<Int>>(any()) } returns mockIntStream
        every { mockIntStream.findFirst() } returns mockOptional
        every { mockOptional.orElse(any()) } returns expected

        assertThat(mockCrawler.crawlPageAsync(uri, 2)).isSameAs(expected)

        verify(exactly = 1) {
            Stream.of(uri)
            mockUriStream.filter(any())
            mockUriStream.map<CompletableFuture<Int>>(any())
            mockIntStream.findFirst()
            mockOptional.orElse(any())
        }
        confirmVerified(mockIntStream, mockUriStream, mockOptional, expected)
    }

    @Test
    fun `performCrawl() stops when maximum depth has been reached`() {
        val uri = "mock"

        mockCrawler.mMaxDepth = 1
        mockCrawler.mUniqueUris = mockUniqueUris

        every {
            mockCrawler.crawlPageHelper(any(), any())
        } throws (RuntimeException("crawlPageHelper should not be called!"))

        every {
            mockCrawler.mUniqueUris.add(any())
        } throws (RuntimeException("mUniqueUris.putIfAbsent() should not be called!"))

        assertThat(mockCrawler.performCrawl(uri, 2)).isEqualTo(0)

        verify(exactly = 0) { mockCrawler.crawlPageHelper(any(), any()) }
        confirmVerified(mockUniqueUris)
    }

    @Test
    fun `performCrawl() has an acceptable solution`() {
        val uri = "mock"
        val cf = mockk<CompletableFuture<Int>>()
        val s1 = mockk<Stream<String>>()
        val s2 = mockk<Stream<CompletableFuture<Int>>>()
        val s3 = mockk<Stream<Int>>()
        val o1 = mockk<Optional<Int>>()
        every { o1.orElse(0) } returns -88
        every { cf.join() } returns -88
        every { s3.findFirst() } returns o1
        mockkStatic(Stream::class)
        every { s2.map<Int>(any()) } returns s3
        every { Stream.of<String>(any()) } returns s1
        every { mockCrawler.crawlPageAsync(uri, -99) } returns cf
        every { s1.map<CompletableFuture<Int>>(any()) } returns s2

        assertThat(mockCrawler.performCrawl(uri, -99)).isEqualTo(-88)
        verifyOneOf(
            "Solution is not valid",
            {
                verify(exactly = 1) {
                    cf.join()
                    mockCrawler.crawlPageAsync(uri, -99)
                }
            },
            {
                verify(exactly = 1) {
                    o1.orElse(any())
                    s1.map<CompletableFuture<Int>>(any())
                    s3.findFirst()
                    Stream.of<String>(any())
                    s2.map<Int>(any())
                }
            }
        )
    }

    @Test
    fun `crawlPageAsync() runs for all valid depths`() {
        val uri = "mock"
        val mockFuture = mockk<CompletableFuture<Int>>()
        mockCrawler.mMaxDepth = 3
        mockCrawler.mUniqueUris = mockUniqueUris

        every { mockCrawler.crawlPageHelper(any(), any()) } answers { mockFuture }
        every { mockCrawler.mUniqueUris.add(any()) } answers { true }

        repeat(mockCrawler.mMaxDepth) {
            mockCrawler.crawlPageAsync(uri, it + 1)
        }

        verify(exactly = mockCrawler.mMaxDepth) {
            mockCrawler.crawlPageAsync(any(), any())
            mockUniqueUris.add(any())
            mockCrawler.crawlPageHelper(any(), any())
        }

        confirmVerified(mockUniqueUris)
    }

    @Test
    fun `crawlPageAsync() ignores previously cached uris`() {
        val uri = "mock"

        mockCrawler.mMaxDepth = 3
        mockCrawler.mUniqueUris = mockk<ConcurrentHashMap.KeySetView<String, Boolean>>()
        val expected = CompletableFuturesCrawler.mZero

        every {
            mockCrawler.crawlPageHelper(any(), any())
        } throws (RuntimeException("crawlPage should not be called!"))

        every { mockCrawler.mUniqueUris.add(uri) } returns false

        assertThat(mockCrawler.crawlPageAsync(uri, 3)).isEqualTo(expected)

        verify(exactly = 1) { mockCrawler.mUniqueUris.add(uri) }
        confirmVerified(mockCrawler.mUniqueUris)
    }

    @Test
    fun `crawlPageAsync() build correct return value`() {
        val mockFuture = mockk<CompletableFuture<Crawler.Page>>()
        val mockIntFuture = mockk<CompletableFuture<Int>>()
        val mockIntFuture2 = mockk<CompletableFuture<Int>>()
        val expected = mockk<CompletableFuture<Int>>()

        every { mockCrawler.getPageAsync("mock") } returns mockFuture
        every { mockCrawler.getImagesOnPageAsync(mockFuture) } returns mockIntFuture
        every { mockCrawler.crawlHyperLinksOnPageAsync(mockFuture, any()) } answers {
            assertThat(arg<Int>(1)).describedAs("incorrect depth parameter").isEqualTo(-98)
            mockIntFuture2
        }
        every { mockCrawler.combineResults(mockIntFuture, mockIntFuture2) } returns expected

        mockCrawler.crawlPageHelper("mock", -99)
        verify(exactly = 1) {
            mockCrawler.getPageAsync("mock")
            mockCrawler.getImagesOnPageAsync(mockFuture)
            mockCrawler.crawlHyperLinksOnPageAsync(mockFuture, any())
        }

        verifyOneOf(
            "combineResults call is not valid",
            {
                verify(exactly = 1) {
                    mockCrawler.combineResults(mockIntFuture, mockIntFuture2)
                }
            },
            {
                verify(exactly = 1) {
                    mockCrawler.combineResults(mockIntFuture2, mockIntFuture)
                }
            }
        )

        confirmVerified(mockFuture, mockIntFuture, expected)
    }

    @Test
    fun `Class members must be initialized`() {
        assertThat(CompletableFuturesCrawler.mZero.get()).isEqualTo(0)
    }

    @Test
    fun `getPageAsync() has correct solution`() {
        val url = "mock"
        val mockPage = mockk<Crawler.Page>()
        mockCrawler.mWebPageCrawler = mockWebPageCrawler
        every { mockWebPageCrawler.getPage(url) } returns mockPage
    }

    @Test
    fun `getPageAsync() is efficient`() {
        mockCrawler.mWebPageCrawler = mockWebPageCrawler
        mockkStatic(CompletableFuture::class)
        every { CompletableFuture.supplyAsync<Int>(any()) } returns mockIntFuture
        assertThat(mockCrawler.getPageAsync("https://www.no.where")).isNotNull
        verify(exactly = 1) { CompletableFuture.supplyAsync<Int>(any()) }
        verify(exactly = 0) { (mockIntFuture.thenApply<Int>(any())) }
    }

    @Test
    fun `getImagesOnPageAsync() returns the correct result`() {
        val mockFuture = mockk<CompletableFuture<Crawler.Page>>()
        val mockUrlsFuture = mockk<CompletableFuture<List<URL>>>()
        val expected = mockk<CompletableFuture<Void>>()

        every { mockFuture.thenApplyAsync<List<URL>>(any()) } returns mockUrlsFuture
        every { mockUrlsFuture.thenComposeAsync<Void>(any()) } returns expected
        assertThat(mockCrawler.getImagesOnPageAsync(mockFuture)).isSameAs(expected)

        verifyAll {
            mockFuture.thenApplyAsync<List<URL>>(any())
            mockUrlsFuture.thenComposeAsync<Void>(any())
        }

        confirmVerified(mockFuture, mockUrlsFuture, expected)
    }

    @Test
    fun     `crawlHyperLinksOnPage() uses expected chain calls and lambdas`() {
        mockkStatic(FuturesCollectorIntStream::class)
        val m0 = ""
        val m5 = mockk<Stream<CompletableFuture<Int>>>()
        val m3 = mockk<CompletableFuture<Int>>()
        val m8 = mockk<Stream<String>>()
        val m9 = mockk<CompletableFuture<IntStream>>()
        val m6 = mockk<Collector<CompletableFuture<Int>, Any, CompletableFuture<IntStream>>>()

        every { mockCrawler.crawlPageAsync(m0, -99) } returns m3
        every { m5.collect<CompletableFuture<IntStream>, CompletableFuture<Int>>(any()) } returns m9
        every { m8.map<CompletableFuture<Int>>(any()) } answers {
            val arg = arg<Function<String, CompletableFuture<Int>>>(0)
            arg.apply(m0)
            m5
        }
        every { mockPage.getPageElementsAsStringStream(any()) } answers {
            assertThat(args.size).isEqualTo(1)
            assertThat(arg<Array<Crawler.Type>>(0)).hasSize(1)
            assertThat(arg<Array<Crawler.Type>>(0)[0]).isNotEqualTo(Crawler.Type.IMAGE)
            m8
        }
        every { FuturesCollectorIntStream.toFuture() } returns m6
        every { m9.thenApply<Int>(any()) } returns m3

        assertThat(mockCrawler.crawlHyperLinksOnPage(mockPage, -99)).isSameAs(m3)

        verify(exactly = 1) {
            m5.collect<CompletableFuture<IntStream>, CompletableFuture<Int>>(any())
            FuturesCollectorIntStream.toFuture()
            m9.thenApply<Int>(any())
            mockPage.getPageElementsAsStringStream(any())
            m8.map<CompletableFuture<Int>>(any())
            mockCrawler.crawlPageAsync(m0, -99)
        }
    }

    @Test
    fun transformImageAsync() {
        val m3 = mockk<CompletableFuture<Image>>()
        val m7 = mockk<CompletableFuture<Image>>()
        val m0 = mockk<Stream<CompletableFuture<Image>>>()
        val m1 = mockk<Stream<Transform>>()
        val m4 = mockk<Transform>()
        val m6 = mockk<List<Transform>>()
        mockCrawler.mTransforms = m6

        every { m3.thenApplyAsync<Image>(any()) } returns m7
        every { m6.stream() } returns m1
        every { m1.map<CompletableFuture<Image>>(any()) } answers {
            arg<Function<Transform, CompletableFuture<Image>>>(0).apply(m4)
            m0
        }
        assertThat(mockCrawler.transformImageAsync(m3)).isSameAs(m0)
        verify(exactly = 1) {
            m3.thenApplyAsync<Image>(any())
            m6.stream()
            m1.map<CompletableFuture<Image>>(any())
        }
    }

    @Test
    fun `crawlHyperLinksOnPageAsync() uses the correct lambda`() {
        val mockPageFuture = mockk<CompletableFuture<Crawler.Page>>()
        val expected = mockk<CompletableFuture<Int>>()

        every { mockPageFuture.thenComposeAsync<Int>(any()) } answers {
            invocation.originalCall()
            val arg = arg<Function<Crawler.Page, CompletableFuture<Int>>>(0)
            arg.apply(mockPage)
        }
        every { mockCrawler.crawlHyperLinksOnPage(mockPage, -99) } answers {
            expected
        }
        assertThat(mockCrawler.crawlHyperLinksOnPageAsync(mockPageFuture, -99)).isSameAs(expected)
        verify(exactly = 1) {
            mockPageFuture.thenComposeAsync<Void>(any())
            mockCrawler.crawlHyperLinksOnPage(mockPage, -99)
        }
    }

    @Test
    fun `crawlHyperLinksOnPageAsync() returns the correct result`() {
        val m1 = mockk<CompletableFuture<Crawler.Page>>()
        val m0 = mockk<CompletableFuture<Int>>()

        every { m1.thenComposeAsync<Int>(any()) } returns m0
        assertThat(mockCrawler.crawlHyperLinksOnPageAsync(m1, -99)).isSameAs(m0)
        verify(exactly = 1) { m1.thenComposeAsync<Void>(any()) }
        confirmVerified(m1, m0)
    }

    @Test
    fun `combineResults() returns the correct result`() {
        val m3 = mockk<CompletableFuture<Int>>()
        val m1 = mockk<CompletableFuture<Int>>()
        val m2 = mockk<CompletableFuture<Int>>()
        every { m3.thenCombine<Int, Int>(m1, any()) } returns m2
        mockCrawler.combineResults(m3, m1)
        verify(exactly = 1) { m3.thenCombine<Int, Int>(m1, any()) }
    }

    @Test
    fun `transformImage uses expected chain calls and lambdas`() {
        mockkStatic(Stream::class)
        val m7 = mockk<Image>()
        val m3 = mockk<Transform>()
        val m2 = mockk<Stream<Transform>>()
        val m6 = mockk<Stream<Transform>>()
        val m1 = mockk<Stream<Image>>()
        val m4 = mockk<Stream<Image>>()

        every { m1.filter(any()) } answers {
            assertThat(arg<Predicate<Transform?>>(0).test(null)).isFalse
            assertThat(arg<Predicate<Transform?>>(0).test(m3)).isTrue
            m4
        }
        every { Stream.of(m3) } returns m2
        every { m1.filter(any()) } returns m4
        every { mockCrawler.applyTransform(any(), any()) } returns m7
        every { m2.filter(any()) } answers {
            arg<Predicate<Transform>>(0).test(m3); m6
        }
        every { m4.count() } returns -99
        every { m6.map<Image>(any()) } answers {
            arg<Function<Transform, Image>>(0).apply(m3); m1
        }
        every { mockCrawler.createNewCacheItem(m7, m3) } returns true
        assertThat(mockCrawler.transformImage(m3, m7)).isSameAs(-99)
        verify(exactly = 1) {
            m4.count()
            m2.filter(any())
            m6.map<Image>(any())
            mockCrawler.applyTransform(any(), any())
            m1.filter(any())
            Stream.of(m3)
            mockCrawler.createNewCacheItem(m7, m3)
        }
    }
}
package edu.vanderbilt.imagecrawler.crawlers

import admin.AssignmentTests
import edu.vanderbilt.imagecrawler.platform.Cache
import edu.vanderbilt.imagecrawler.transforms.Transform
import edu.vanderbilt.imagecrawler.utils.BlockingTask
import edu.vanderbilt.imagecrawler.utils.FuturesCollectorIntStream
import edu.vanderbilt.imagecrawler.utils.Image
import edu.vanderbilt.imagecrawler.web.WebPageCrawler
import io.mockk.*
import io.mockk.impl.annotations.MockK
import io.mockk.impl.annotations.SpyK
import org.assertj.core.api.Assertions.assertThat
import org.junit.Before
import org.junit.Test
import java.net.URL
import java.util.*
import java.util.concurrent.CompletableFuture
import java.util.concurrent.CompletionStage
import java.util.concurrent.ConcurrentHashMap
import java.util.function.Function
import java.util.function.Predicate
import java.util.function.Supplier
import java.util.stream.Collector
import java.util.stream.IntStream
import java.util.stream.Stream

class CompletableFuturesCrawlerTests : AssignmentTests() {
    @SpyK
    var crawler = CompletableFuturesCrawler()

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
        every { crawler.log(any(), *anyVararg()) } answers { }
        every { crawler.runLocalTransforms() } returns true
        every { crawler.runRemoteTransforms() } returns false
    }

    @Test
    fun `Class members must be initialized`() {
        assertThat(CompletableFuturesCrawler.mZero.get()).isEqualTo(0)
    }

    @Test
    fun `performCrawl() stops when maximum depth has been reached`() {
        val uri = "mock"

        crawler.mMaxDepth = 1
        crawler.mUniqueUris = mockUniqueUris

        every {
            crawler.crawlPageHelper(any(), any())
        } throws (RuntimeException("crawlPageHelper should not be called!"))

        every {
            crawler.mUniqueUris.add(any())
        } throws (RuntimeException("mUniqueUris.putIfAbsent() should not be called!"))

        assertThat(crawler.performCrawl(uri, 2)).isEqualTo(0)

        verify(exactly = 0) { crawler.crawlPageHelper(any(), any()) }
        confirmVerified(mockUniqueUris)
    }

    @Test
    fun `performCrawl() has an acceptable solution`() {
        val uri = "mock"
        val cf = mockk<CompletableFuture<Int>>()
        every { cf.join() } returns -88
        every { crawler.crawlPageAsync(uri, -99) } returns cf
        assertThat(crawler.performCrawl(uri, -99)).isEqualTo(-88)
        verify(exactly = 1) {
            cf.join()
            crawler.crawlPageAsync(uri, -99)
            crawler.performCrawl(any(), any())
        }
        confirmVerified(cf, crawler)
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

        assertThat(crawler.crawlPageAsync(uri, 2)).isSameAs(expected)

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
    fun `crawlPageAsync() runs for all valid depths`() {
        val uri = "mock"
        val mockFuture = mockk<CompletableFuture<Int>>()
        crawler.mMaxDepth = 3
        crawler.mUniqueUris = mockUniqueUris

        every { crawler.crawlPageHelper(any(), any()) } answers { mockFuture }
        every { crawler.mUniqueUris.add(any()) } answers { true }

        repeat(crawler.mMaxDepth) {
            crawler.crawlPageAsync(uri, it + 1)
        }

        verify(exactly = crawler.mMaxDepth) {
            crawler.crawlPageAsync(any(), any())
            mockUniqueUris.add(any())
            crawler.crawlPageHelper(any(), any())
            crawler.log(any())
        }

        confirmVerified(crawler, mockUniqueUris)
    }

    @Test
    fun `crawlPageAsync() ignores previously cached uris`() {
        val uri = "mock"

        crawler.mMaxDepth = 3
        crawler.mUniqueUris = mockk<ConcurrentHashMap.KeySetView<String, Boolean>>()
        val expected = CompletableFuturesCrawler.mZero

        every {
            crawler.crawlPageHelper(any(), any())
        } throws (RuntimeException("crawlPage should not be called!"))

        every { crawler.mUniqueUris.add(uri) } returns false

        assertThat(crawler.crawlPageAsync(uri, 3)).isEqualTo(expected)

        verify(exactly = 1) {
            crawler.mUniqueUris.add(uri)
            crawler.crawlPageAsync(any(), any())
            crawler.log(any())
        }
        confirmVerified(crawler, crawler.mUniqueUris)
    }

    @Test
    fun `getPageAsync() has correct solution`() {
        val cfp = mockk<CompletableFuture<Crawler.Page>>()
        val cps = mockk<Crawler.Page>()
        mockkStatic(CompletableFuture::class)
        crawler.mWebPageCrawler = mockk()
        every {
            CompletableFuture.supplyAsync(any<Supplier<Crawler.Page>>())
        } answers {
            firstArg<Supplier<Crawler.Page>>().get()
            cfp
        }
        every {
            crawler.callInManagedBlocker<Crawler.Page>(any())
        } answers {
            firstArg<Supplier<Crawler.Page>>().get()
            cps
        }
        every { crawler.mWebPageCrawler.getPage(any()) } answers { cps }

        assertThat(crawler.getPageAsync("mock")).isSameAs(cfp)

        verify(exactly = 1) {
            CompletableFuture.supplyAsync(any<Supplier<Crawler.Page>>())
            crawler.callInManagedBlocker<Crawler.Page>(any())
            crawler.mWebPageCrawler.getPage(any())
            crawler.getPageAsync("mock")
        }
        confirmVerified(crawler)
    }

    @Test
    fun `getPageAsync() is efficient`() {
        crawler.mWebPageCrawler = mockWebPageCrawler
        mockkStatic(CompletableFuture::class)
        every { CompletableFuture.supplyAsync<Int>(any()) } returns mockIntFuture
        assertThat(crawler.getPageAsync("https://www.no.where")).isNotNull
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
        assertThat(crawler.getImagesOnPageAsync(mockFuture)).isSameAs(expected)

        verifyAll {
            mockFuture.thenApplyAsync<List<URL>>(any())
            mockUrlsFuture.thenComposeAsync<Void>(any())
        }

        confirmVerified(mockFuture, mockUrlsFuture, expected)
    }

    @Test
    fun crawlHyperLinksOnPageAsync() {
        val cfp = mockk<CompletableFuture<Crawler.Page>>()
        val d = 99
        val cfi = mockk<CompletableFuture<Int>>()
        every {
            cfp.thenComposeAsync(any<Function<Crawler.Page, CompletionStage<Int>>>())
        } answers {
            firstArg<Function<Crawler.Page, CompletionStage<Int>>>().apply(mockk())
            cfi
        }
        every { crawler.crawlHyperLinksOnPage(any(), any()) } answers { cfi }
        assertThat(crawler.crawlHyperLinksOnPageAsync(cfp, d)).isSameAs(cfi)
        verify(exactly = 1) {
            cfp.thenComposeAsync(any<Function<Crawler.Page, CompletionStage<Int>>>())
            crawler.crawlHyperLinksOnPage(any(), any())
            crawler.crawlHyperLinksOnPageAsync(cfp, d)
        }
        confirmVerified(cfp, cfi, crawler)
    }

    @Test
    fun `crawlHyperLinksOnPage() uses expected chain calls and lambdas`() {
        mockkStatic(FuturesCollectorIntStream::class)
        val m0 = ""
        val m5 = mockk<Stream<CompletableFuture<Int>>>()
        val m2 = mockk<IntStream>()
        val m3 = mockk<CompletableFuture<Int>>()
        val m8 = mockk<Stream<String>>()
        val m9 = mockk<CompletableFuture<IntStream>>()
        val m6 = mockk<Collector<CompletableFuture<Int>, Any, CompletableFuture<IntStream>>>()

        every { m2.sum() } returns 88
        every { crawler.crawlPageAsync(m0, -99) } returns m3
        every { m9.thenApply<Int>(any()) } answers {
            assertThat(firstArg<Function<IntStream, Int>>().apply(m2)).isEqualTo(88)
            m3
        }
        every { m5.collect<CompletableFuture<IntStream>, CompletableFuture<Int>>(any()) } returns m9
        every { m8.map<CompletableFuture<Int>>(any()) } answers {
            firstArg<Function<String, CompletableFuture<Int>>>().apply(m0)
            m5
        }
        every { mockPage.getPageElementsAsStringStream(*anyVararg()) } answers {
            assertThat(args.size).isEqualTo(1)
            assertThat(firstArg<Array<Crawler.Type>>()).hasSize(1)
            assertThat(firstArg<Array<Crawler.Type>>()[0]).isNotEqualTo(Crawler.Type.IMAGE)
            m8
        }
        every { FuturesCollectorIntStream.toFuture() } returns m6

        assertThat(crawler.crawlHyperLinksOnPage(mockPage, -99)).isSameAs(m3)

        verify(exactly = 1) {
            m5.collect<CompletableFuture<IntStream>, CompletableFuture<Int>>(any())
            FuturesCollectorIntStream.toFuture()
            m2.sum()
            m9.thenApply<Int>(any())
            mockPage.getPageElementsAsStringStream(*anyVararg())
            m8.map<CompletableFuture<Int>>(any())
            crawler.crawlHyperLinksOnPage(any(), any())
            crawler.crawlPageAsync(m0, -99)
        }

        confirmVerified(m2, m3, m5, m6, m8, m9, crawler, mockPage)
    }

    @Test
    fun transformImageAsync() {
        val m3 = mockk<CompletableFuture<Image>>()
        val m7 = mockk<CompletableFuture<Image>>()
        val m0 = mockk<Stream<CompletableFuture<Image>>>()
        val m1 = mockk<Stream<Transform>>()
        val m4 = mockk<Transform>()
        val m6 = mockk<List<Transform>>()
        crawler.mTransforms = m6

        every { m3.thenApplyAsync<Image>(any()) } returns m7
        every { m6.stream() } returns m1
        every { m1.map<CompletableFuture<Image>>(any()) } answers {
            firstArg<Function<Transform, CompletableFuture<Image>>>().apply(m4)
            m0
        }
        assertThat(crawler.transformImageAsync(m3)).isSameAs(m0)
        verify(exactly = 1) {
            m3.thenApplyAsync<Image>(any())
            m6.stream()
            m1.map<CompletableFuture<Image>>(any())
            crawler.transformImageAsync(m3)
        }
        confirmVerified(m3, m6, m1, m4, m0, m7, crawler)
    }

    @Test
    fun `crawlHyperLinksOnPageAsync() uses the correct lambda`() {
        val pf = mockk<CompletableFuture<Crawler.Page>>()
        val expected = mockk<CompletableFuture<Int>>()

        every { pf.thenComposeAsync<Int>(any()) } answers {
            firstArg<Function<Crawler.Page, CompletableFuture<Int>>>().apply(mockPage)
        }
        every { crawler.crawlHyperLinksOnPage(mockPage, -99) } answers {
            expected
        }
        assertThat(crawler.crawlHyperLinksOnPageAsync(pf, -99)).isSameAs(expected)
        verify(exactly = 1) {
            pf.thenComposeAsync<Void>(any())
            crawler.crawlHyperLinksOnPage(mockPage, -99)
            crawler.crawlHyperLinksOnPageAsync(any(), any())
        }
        confirmVerified(crawler, pf)
    }

    @Test
    fun `crawlHyperLinksOnPageAsync() returns the correct result`() {
        val m1 = mockk<CompletableFuture<Crawler.Page>>()
        val m0 = mockk<CompletableFuture<Int>>()

        every { m1.thenComposeAsync<Int>(any()) } returns m0
        assertThat(crawler.crawlHyperLinksOnPageAsync(m1, -99)).isSameAs(m0)
        verify(exactly = 1) {
            crawler.crawlHyperLinksOnPageAsync(any(), any())
            m1.thenComposeAsync<Void>(any())
        }
        confirmVerified(m1, m0, crawler)
    }

    @Test
    fun `combineResults() returns the correct result`() {
        val m3 = mockk<CompletableFuture<Int>>()
        val m1 = mockk<CompletableFuture<Int>>()
        val m2 = mockk<CompletableFuture<Int>>()
        every { m3.thenCombine<Int, Int>(m1, any()) } returns m2
        crawler.combineResults(m3, m1)
        verify(exactly = 1) {
            crawler.combineResults(any(), any())
            m3.thenCombine<Int, Int>(m1, any())
        }
        confirmVerified(crawler, m2, m3, m1)
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
            assertThat(firstArg<Predicate<Transform?>>().test(null)).isFalse
            assertThat(firstArg<Predicate<Transform?>>().test(m3)).isTrue
            m4
        }
        every { Stream.of(m3) } returns m2
        every { m1.filter(any()) } returns m4
        every { crawler.applyTransform(any(), any()) } returns m7
        every { m2.filter(any()) } answers {
            firstArg<Predicate<Transform>>().test(m3); m6
        }
        every { m4.count() } returns -99
        every { m6.map<Image>(any()) } answers {
            firstArg<Function<Transform, Image>>().apply(m3); m1
        }
        every { crawler.createNewCacheItem(m7, m3) } returns true
        assertThat(crawler.transformImage(m3, m7)).isSameAs(-99)
        verify(exactly = 1) {
            m4.count()
            m2.filter(any())
            m6.map<Image>(any())
            crawler.applyTransform(any(), any())
            m1.filter(any())
            Stream.of(m3)
            crawler.createNewCacheItem(m7, m3)
            crawler.transformImage(any(), any())
        }
        confirmVerified(crawler, m4, m2, m6, m1)
    }

    @Test
    fun `crawlPageHelper produces the correct result`() {
        val mockFuture = mockk<CompletableFuture<Crawler.Page>>()
        val mockIntFuture = mockk<CompletableFuture<Int>>()
        val mockIntFuture2 = mockk<CompletableFuture<Int>>()
        val expected = mockk<CompletableFuture<Int>>()

        every { crawler.getPageAsync("mock") } returns mockFuture
        every { crawler.getImagesOnPageAsync(mockFuture) } returns mockIntFuture
        every { crawler.crawlHyperLinksOnPageAsync(mockFuture, any()) } answers {
            assertThat(arg<Int>(1)).describedAs("incorrect depth parameter").isEqualTo(-98)
            mockIntFuture2
        }
        every { crawler.combineResults(mockIntFuture, mockIntFuture2) } returns expected

        crawler.crawlPageHelper("mock", -99)
        verify(exactly = 1) {
            crawler.getPageAsync("mock")
            crawler.getImagesOnPageAsync(mockFuture)
            crawler.crawlHyperLinksOnPageAsync(mockFuture, any())
            crawler.combineResults(any(), any())
            crawler.crawlPageHelper(any(), any())
        }

        verifyOneOf(
            "combineResults call is not valid",
            {
                verify(exactly = 1) {
                    crawler.combineResults(mockIntFuture, mockIntFuture2)
                }
            },
            {
                verify(exactly = 1) {
                    crawler.combineResults(mockIntFuture2, mockIntFuture)
                }
            }
        )

        confirmVerified(crawler, mockFuture, mockIntFuture, expected)
    }

    // Alternate not currently used.
    //@Test
    fun `crawlPageHelper uses the correct chain calls and inner lambdas`() {
        val i1 = "mock"
        val i2 = 99
        val cfis = mockk<Stream<CompletableFuture<Int>>>()
        val cfp = mockk<CompletableFuture<Crawler.Page>>()
        val ocfi = mockk<Optional<CompletableFuture<Int>>>()
        val ss = mockk<Stream<String>>()
        val cfiz = mockk<CompletableFuture<Int>>()
        val cfs = mockk<Stream<CompletableFuture<Crawler.Page>>>()
        mockkStatic(Stream::class)

        every { cfis.findFirst() } answers { ocfi }
        every { crawler.getPageAsync(any()) } answers { cfp }
        every { crawler.crawlHyperLinksOnPageAsync(any(), 100) } answers { mockk() }
        every { Stream.of<String>(any()) } answers { ss }
        every { ocfi.orElse(any()) } answers { cfiz }
        every { crawler.getImagesOnPageAsync(any()) } answers { mockk() }
        every { ss.map<CompletableFuture<Crawler.Page>>(any()) } answers {
            firstArg<Function<String, Crawler.Page>>().apply("mock")
            cfs
        }
        every { crawler.combineResults(any(), any()) } answers { mockk() }
        every { cfs.map<CompletableFuture<Int>>(any()) } answers {
            firstArg<Function<CompletableFuture<Crawler.Page>,
                    CompletableFuture<Int>>>().apply(mockk())
            cfis
        }

        assertThat(crawler.crawlPageHelper(i1, i2)).isSameAs(cfiz)

        verify(exactly = 1) {
            crawler.getPageAsync(any())
            cfis.findFirst()
            crawler.getImagesOnPageAsync(any())
            cfs.map<CompletableFuture<Int>>(any())
            crawler.combineResults(any(), any())
            ocfi.orElse(any())
            crawler.crawlHyperLinksOnPageAsync(any(), 100)
            Stream.of<String>(any())
            ss.map<CompletableFuture<Crawler.Page>>(any())
            crawler.crawlPageHelper(any(), any())
        }

        confirmVerified(crawler, cfis, cfs, ocfi, ss)
    }

    @Test
    fun `callInManagedBlocker is implemented correctly`() {
        val sa = mockk<Supplier<Any>>()
        val a = mockk<Any>()
        mockkStatic(BlockingTask::class)
        every { BlockingTask.callInManagedBlock(any<Supplier<Any>>()) } returns a
        crawler.callInManagedBlocker(sa)
        verify {
            crawler.callInManagedBlocker(sa)
            BlockingTask.callInManagedBlock(sa)
        }
        confirmVerified(crawler, sa, a)
    }

    @Test
    fun `managedBlockerDownloadImage is implemented correctly`() {
        val ca = mockk<Cache.Item>()
        val mi = mockk<Image>()
        every { crawler.callInManagedBlocker(any<Supplier<Image>>()) } answers {
            firstArg<Supplier<Image>>().get()
        }
        every { crawler.downloadImage(ca) } answers { mi }
        assertThat(crawler.managedBlockerDownloadImage(ca)).isSameAs(mi)
        verify {
            crawler.callInManagedBlocker(any<Supplier<Image>>())
            crawler.managedBlockerDownloadImage(any())
            crawler.downloadImage(ca)
        }
    }
}
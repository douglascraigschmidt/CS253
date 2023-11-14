package edu.vanderbilt.imagecrawler.crawlers

import admin.AssignmentTests
import admin.findField
import edu.vanderbilt.imagecrawler.platform.Cache
import edu.vanderbilt.imagecrawler.transforms.Transform
import edu.vanderbilt.imagecrawler.utils.BlockingTask
import edu.vanderbilt.imagecrawler.utils.FuturesCollectorIntStream
import edu.vanderbilt.imagecrawler.utils.Image
import io.mockk.MockKAnnotations
import io.mockk.confirmVerified
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.impl.annotations.SpyK
import io.mockk.mockk
import io.mockk.mockkStatic
import io.mockk.verify
import io.mockk.verifyAll
import org.assertj.core.api.Assertions.assertThat
import org.junit.Before
import org.junit.Test
import java.net.URL
import java.util.Optional
import java.util.concurrent.CompletableFuture
import java.util.concurrent.CompletionStage
import java.util.concurrent.ConcurrentHashMap
import java.util.function.BiConsumer
import java.util.function.Consumer
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
        val f = CompletableFuturesCrawler::class.java.findField(CompletableFuture::class.java)
        assertThat(f).isNotNull
        val v = assertThat(f.get(crawler))
        assertThat(v).isNotNull
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
    fun `crawlPageAsync() uses expected chained method calls and lambdas`() {
        mockkStatic(Stream::class)
        val uri = "mock"
        val fi = mockk<CompletableFuture<Int>>()
        val fp = mockk<CompletableFuture<Crawler.Page>>()
        val sfi = mockk<Stream<CompletableFuture<Int>>>()
        val us = mockk<Stream<String>>()
        val ofi = mockk<Optional<CompletableFuture<Int>>>()
        val sfp = mockk<Stream<CompletableFuture<Crawler.Page>>>()
        val expected = mockk<CompletableFuture<Int>>()

        every { crawler.imagesOnPageAndPageLinksAsync(any(), any()) } answers { fi }
        every { ofi.orElse(any()) } returns expected
        every { us.map<CompletableFuture<Crawler.Page>>(any()) } answers {
            firstArg<Function<String, CompletableFuture<Crawler.Page>>>().apply(".")
            sfp
        }
        every { us.filter(any()) } returns us
        every { Stream.of(uri) } returns us
        every { sfi.findFirst() } returns ofi
        every { crawler.getPageAsync(any()) } answers { fp }
        every { sfp.map<CompletableFuture<Int>>(any()) } answers {
            firstArg<Function<CompletableFuture<Crawler.Page>, CompletableFuture<Int>>>().apply(
                mockk()
            )
            sfi
        }

        assertThat(crawler.crawlPageAsync(uri, 2)).isSameAs(expected)

        verify(exactly = 1) {
            crawler.imagesOnPageAndPageLinksAsync(any(), any())
            Stream.of(uri)
            ofi.orElse(any())
            sfp.map<CompletableFuture<Int>>(any())
            us.map<CompletableFuture<Crawler.Page>>(any())
            sfi.findFirst()
            crawler.getPageAsync(any())
            us.filter(any())
        }
        confirmVerified(sfi, sfp, us, ofi, expected)
    }

    @Test
    fun `crawlPageAsync() runs for all valid depths`() {
        val uri = "mock"
        val fi = mockk<CompletableFuture<Int>>()
        val fp = mockk<CompletableFuture<Crawler.Page>>()
        val uu = mockk<ConcurrentHashMap.KeySetView<String, Boolean>>()
        crawler.mMaxDepth = 3
        crawler.mUniqueUris = uu

        every { crawler.imagesOnPageAndPageLinksAsync(any(), any()) } answers { fi }
        every { crawler.getPageAsync(any()) } answers { fp }
        every { crawler.mUniqueUris.add(any()) } answers { true }

        repeat(crawler.mMaxDepth) {
            crawler.crawlPageAsync(uri, it + 1)
        }

        verify(exactly = crawler.mMaxDepth) {
            crawler.crawlPageAsync(any(), any())
            uu.add(any())
            crawler.getPageAsync(any())
            crawler.imagesOnPageAndPageLinksAsync(any(), any())
            crawler.log(any())
        }

        confirmVerified(crawler, uu)
    }

    @Test
    fun `crawlPageAsync() ignores previously cached uris`() {
        val uri = "mock"

        crawler.mMaxDepth = 3
        crawler.mUniqueUris = mockk<ConcurrentHashMap.KeySetView<String, Boolean>>()
        val expected =
            CompletableFuturesCrawler::class.java.findField(
                CompletableFuture::class.java
            ).get(crawler)

        every {
            crawler.imagesOnPageAndPageLinksAsync(any(), any())
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
    fun imagesOnPageAndPageLinksAsync() {
        val i1 = mockk<CompletableFuture<Crawler.Page>>()
        val i2 = 99
        val m1 = mockk<CompletableFuture<Int>>()
        val m2 = mockk<CompletableFuture<Int>>()
        val e1 = mockk<CompletableFuture<Int>>()
        every { crawler.imagesOnPageAsync(i1) } answers { m1 }
        every { crawler.imagesOnPageLinksAsync(i1, i2 + 1) } answers { m2 }
        every { crawler.combineResults(m1, m2) } answers { e1 }
        assertThat(crawler.imagesOnPageAndPageLinksAsync(i1, i2)).isSameAs(e1)

        verify(exactly = 1) {
            crawler.imagesOnPageAsync(i1)
            crawler.imagesOnPageLinksAsync(i1, i2 + 1)
            crawler.combineResults(m1, m2)
            crawler.imagesOnPageAndPageLinksAsync(i1, i2)
        }
    }

    @Test
    fun `imagesOnPageAndPageLinksAsync produces the correct result`() {
        val fp = mockk<CompletableFuture<Crawler.Page>>()
        val fi = mockk<CompletableFuture<Int>>()
        val fi2 = mockk<CompletableFuture<Int>>()
        val e = mockk<CompletableFuture<Int>>()

        every { crawler.imagesOnPageAsync(fp) } returns fi
        every { crawler.imagesOnPageLinksAsync(fp, any()) } answers {
            assertThat(secondArg<Int>()).describedAs("incorrect depth parameter").isEqualTo(-98)
            fi2
        }
        every { crawler.combineResults(fi, fi2) } returns e

        crawler.imagesOnPageAndPageLinksAsync(fp, -99)

        verify(exactly = 1) {
            crawler.imagesOnPageAsync(fp)
            crawler.imagesOnPageLinksAsync(fp, any())
            crawler.combineResults(any(), any())
            crawler.imagesOnPageAndPageLinksAsync(fp, -99)
        }

        verifyOneOf(
            "combineResults call is not valid",
            { verify(exactly = 1) { crawler.combineResults(fi, fi2) } },
            { verify(exactly = 1) { crawler.combineResults(fi2, fi) } }
        )

        confirmVerified(crawler, fp, fi, fi2, e)
    }

    @Test
    fun `getPageAsync() is efficient`() {
        crawler.mWebPageCrawler = mockk()
        mockkStatic(CompletableFuture::class)
        val fi = mockk<CompletableFuture<Int>>()
        every { CompletableFuture.supplyAsync<Int>(any()) } returns fi
        assertThat(crawler.getPageAsync("https://www.no.where")).isNotNull
        verify(exactly = 1) { CompletableFuture.supplyAsync<Int>(any()) }
        verify(exactly = 0) { (fi.thenApply<Int>(any())) }
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
    fun `imagesOnPageAsync() returns the correct result`() {
        val mockFuture = mockk<CompletableFuture<Crawler.Page>>()
        val mockUrlsFuture = mockk<CompletableFuture<List<URL>>>()
        val expected = mockk<CompletableFuture<Void>>()

        every { mockFuture.thenApplyAsync<List<URL>>(any()) } returns mockUrlsFuture
        every { mockUrlsFuture.thenComposeAsync<Void>(any()) } returns expected
        assertThat(crawler.imagesOnPageAsync(mockFuture)).isSameAs(expected)

        verifyAll {
            mockFuture.thenApplyAsync<List<URL>>(any())
            mockUrlsFuture.thenComposeAsync<Void>(any())
        }

        confirmVerified(mockFuture, mockUrlsFuture, expected)
    }

    @Test
    fun imagesOnPageLinksAsync() {
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
        assertThat(crawler.imagesOnPageLinksAsync(cfp, d)).isSameAs(cfi)
        verify(exactly = 1) {
            cfp.thenComposeAsync(any<Function<Crawler.Page, CompletionStage<Int>>>())
            crawler.crawlHyperLinksOnPage(any(), any())
            crawler.imagesOnPageLinksAsync(cfp, d)
        }
        confirmVerified(cfp, cfi, crawler)
    }

    @Test
    fun `imagesOnPageLinksAsync() uses the correct lambda`() {
        val pf = mockk<CompletableFuture<Crawler.Page>>()
        val expected = mockk<CompletableFuture<Int>>()

        every { pf.thenComposeAsync<Int>(any()) } answers {
            firstArg<Function<Crawler.Page, CompletableFuture<Int>>>().apply(mockPage)
        }
        every { crawler.crawlHyperLinksOnPage(mockPage, -99) } answers {
            expected
        }
        assertThat(crawler.imagesOnPageLinksAsync(pf, -99)).isSameAs(expected)
        verify(exactly = 1) {
            pf.thenComposeAsync<Void>(any())
            crawler.crawlHyperLinksOnPage(mockPage, -99)
            crawler.imagesOnPageLinksAsync(any(), any())
        }
        confirmVerified(crawler, pf)
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
    fun processImagesAsync() {
        val scfi = mockk<Stream<CompletableFuture<Int>>>()
        val su = mockk<Stream<URL>>()
        val u = mockk<URL>()
        val scfim = mockk<Stream<CompletableFuture<Image>>>()
        val cfis = mockk<CompletableFuture<IntStream>>()
        val ci = mockk<Cache.Item>()
        val i = mockk<Image>()
        val fcis = mockk<FuturesCollectorIntStream>()
        val cfim = mockk<CompletableFuture<Image>>()
        val cfi = mockk<CompletableFuture<Int>>()
        mockkStatic(FuturesCollectorIntStream::class)
        mockkStatic(IntStream::class)
        every { crawler.transformImageAsync(any()) } answers { scfi }
        every { scfim.flatMap<CompletableFuture<Int>>(any()) } answers {
            firstArg<Function<CompletableFuture<Image>, Stream<out CompletableFuture<Int>>>>().apply(cfim)
            scfi
        }
        every { FuturesCollectorIntStream.toFuture() } returns fcis
        every { cfis.thenApply<Int>(any()) } answers {
            cfi
        }
        every { su.map<CompletableFuture<Image>>(any()) } answers {
            firstArg<Function<URL, CompletableFuture<Image>>>().apply(u)
            scfim
        }
        every { crawler.getOrDownloadImageAsync(any(), any()) } answers {
            secondArg<Consumer<Cache.Item>>().accept(ci)
            cfim
        }
        every { scfi.collect(any<Collector<Any, Any, Any>>()) } answers {
            cfis
        }
        every { crawler.managedBlockerDownloadImage(any()) } answers { i }
        assertThat(crawler.processImagesAsync(su)).isSameAs(cfi)
        verify(exactly = 1) {
            crawler.transformImageAsync(any())
            crawler.managedBlockerDownloadImage(any())
            cfis.thenApply<Int>(any())
            su.map<CompletableFuture<Image>>(any())
            scfim.flatMap<CompletableFuture<Int>>(any())
            crawler.processImagesAsync(su)
            crawler.getOrDownloadImageAsync(any(), any())
            scfi.collect(any<Collector<Any, Any, Any>>())
            FuturesCollectorIntStream.toFuture()
        }
        confirmVerified(
            cfim,
            su,
            crawler,
            scfim,
            ci,
            scfi,
            cfi,
            cfis,
            i,
            fcis,
            u,
        )
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
    fun `crawlHyperLinksOnPageAsync() returns the correct result`() {
        val m1 = mockk<CompletableFuture<Crawler.Page>>()
        val m0 = mockk<CompletableFuture<Int>>()

        every { m1.thenComposeAsync<Int>(any()) } returns m0
        assertThat(crawler.imagesOnPageLinksAsync(m1, -99)).isSameAs(m0)
        verify(exactly = 1) {
            crawler.imagesOnPageLinksAsync(any(), any())
            m1.thenComposeAsync<Void>(any())
        }
        confirmVerified(m1, m0, crawler)
    }

    @Test
    fun `transformImage uses expected chain calls and lambdas`() {
        mockkStatic(Stream::class)
        val m7 = mockk<Image>()
        val m3 = mockk<Transform>()
        val m2 = mockk<Stream<Transform>>()
        val m4 = mockk<Stream<Image>>()
        val m5 = mockk<Consumer<Image>>()

        every { m2.filter(any()) } answers {
            assertThat(firstArg<Predicate<Transform>>().test(m3)).isTrue
            m2
        }
        every { m2.mapMulti<Image>(any()) } answers {
            firstArg<BiConsumer<Transform, Consumer<Image>>>().accept(m3, m5)
            m4
        }
        every { m5.accept(m7) } answers {
            Unit
        }

        every { Stream.of(m3) } returns m2
        every { crawler.applyTransform(any(), any()) } returns m7
        every { m4.count() } answers { -99 }
        every { crawler.createNewCacheItem(m7, m3) } returns true
        assertThat(crawler.transformImage(m3, m7)).isSameAs(-99)
        verify(exactly = 1) {
            m5.accept(m7)
            m2.filter(any())
            m4.count()
            m2.mapMulti<Image>(any())
            crawler.applyTransform(any(), any())
            Stream.of(m3)
            crawler.createNewCacheItem(m7, m3)
            crawler.transformImage(any(), any())
        }
        confirmVerified(crawler, m2, m3, m4, m5, m7)
    }

    @Test
    fun `transformImage handles image cache failure`() {
        mockkStatic(Stream::class)
        val m7 = mockk<Image>()
        val m3 = mockk<Transform>()
        val m2 = mockk<Stream<Transform>>()
        val m4 = mockk<Stream<Image>>()
        val m5 = mockk<Consumer<Image>>()

        every { m2.filter(any()) } answers {
            assertThat(firstArg<Predicate<Transform>>().test(m3)).isFalse
            m2
        }
        every { m2.mapMulti<Image>(any()) } answers {
            firstArg<BiConsumer<Transform, Consumer<Image>>>().accept(m3, m5)
            m4
        }
        every { m5.accept(any()) } answers {
            throw RuntimeException("Accept should not be called.")
        }

        every { Stream.of(m3) } returns m2
        every { crawler.applyTransform(any(), any()) } returns null
        every { m4.count() } answers { -99 }
        every { crawler.createNewCacheItem(m7, m3) } returns false
        assertThat(crawler.transformImage(m3, m7)).isSameAs(-99)
        verify(exactly = 1) {
            m2.filter(any())
            m2.mapMulti<Image>(any())
            crawler.applyTransform(any(), any())
            m4.count()
            Stream.of(m3)
            crawler.createNewCacheItem(m7, m3)
            crawler.transformImage(any(), any())
        }
        verify(exactly = 0) {
            m5.accept(any())
        }
        confirmVerified(crawler, m2, m3, m4, m5, m7)
    }

    @Test
    fun `transformImage handles null images results`() {
        mockkStatic(Stream::class)
        val m7 = mockk<Image>()
        val m3 = mockk<Transform>()
        val m2 = mockk<Stream<Transform>>()
        val m4 = mockk<Stream<Image>>()
        val m5 = mockk<Consumer<Image>>()

        every { m2.filter(any()) } answers {
            assertThat(firstArg<Predicate<Transform>>().test(m3)).isTrue
            m2
        }
        every { m2.mapMulti<Image>(any()) } answers {
            firstArg<BiConsumer<Transform, Consumer<Image>>>().accept(m3, m5)
            m4
        }
        every { m5.accept(any()) } answers {
            throw RuntimeException("Accept should not be called.")
        }

        every { Stream.of(m3) } returns m2
        every { crawler.applyTransform(any(), any()) } returns null
        every { m4.count() } answers { -99 }
        every { crawler.createNewCacheItem(m7, m3) } returns true
        assertThat(crawler.transformImage(m3, m7)).isSameAs(-99)
        verify(exactly = 1) {
            m2.filter(any())
            m4.count()
            m2.mapMulti<Image>(any())
            crawler.applyTransform(any(), any())
            Stream.of(m3)
            crawler.createNewCacheItem(m7, m3)
            crawler.transformImage(any(), any())
        }
        verify(exactly = 0) {
            m5.accept(any())
        }
        confirmVerified(crawler, m2, m3, m4, m5, m7)
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
        confirmVerified(crawler, ca, mi)
    }
}
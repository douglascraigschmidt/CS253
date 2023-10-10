package edu.vanderbilt.imagecrawler.crawlers

import admin.AssignmentTests
import edu.vanderbilt.imagecrawler.platform.Cache
import edu.vanderbilt.imagecrawler.transforms.Transform
import edu.vanderbilt.imagecrawler.utils.BlockingTask
import edu.vanderbilt.imagecrawler.utils.Image
import edu.vanderbilt.imagecrawler.web.WebPageCrawler
import edu.vanderbilt.imagecrawler.web.WebPageElement
import io.mockk.MockKAnnotations
import io.mockk.Runs
import io.mockk.confirmVerified
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.impl.annotations.SpyK
import io.mockk.just
import io.mockk.mockk
import io.mockk.mockkStatic
import io.mockk.verify
import org.assertj.core.api.Assertions.assertThat
import org.junit.Before
import org.junit.Test
import java.net.URL
import java.util.concurrent.ConcurrentHashMap
import java.util.function.BiConsumer
import java.util.function.Consumer
import java.util.function.Function
import java.util.function.Predicate
import java.util.function.Supplier
import java.util.stream.Stream

class ParallelStreamsCrawlerTests : AssignmentTests() {
    @SpyK
    var crawler = ParallelStreamsCrawler()

    @MockK
    lateinit var image: Image

    @Before
    fun before() {
        MockKAnnotations.init(this, relaxUnitFun = true)
        every { crawler.log(any(), *anyVararg()) } answers { }
        every { crawler.runLocalTransforms() } returns true
        every { crawler.runRemoteTransforms() } returns false
    }

    @Test
    fun `performCrawl() uses expected chained method calls`() {
        mockkStatic(Stream::class)
        val ss = mockk<Stream<String>>()
        val si = mockk<Stream<Image>>()
        val so = mockk<Stream<Image>>()
        mockkStatic(Stream::class)

        every { si.forEach(any()) } just Runs
        every { Stream.of<String>(any()) } answers { ss }
        every { crawler.crawlPage(any(), any()) } answers { si }
        every { so.count() } answers { -91 }
        every { ss.mapMulti<Image>(any()) } answers {
            firstArg<BiConsumer<String, Consumer<Stream<Image>>>>().accept("", mockk())
            so
        }
        assertThat(crawler.performCrawl("", -99)).isEqualTo(-91)

        verify(exactly = 1) {
            crawler.performCrawl("", -99)
            so.count()
            si.forEach(any())
            crawler.log(any(), *anyVararg())
            ss.mapMulti<Image>(any())
            crawler.crawlPage(any(), any())
            Stream.of<String>(any())
        }
        confirmVerified(crawler, ss, si, so)
    }

    @Test
    fun `crawlPage() uses expected chained method calls and lambdas`() {
        val ps = mockk<Stream<Crawler.Page>>()
        val si = mockk<Stream<Image>>()
        val ss = mockk<Stream<String>>()
        val bl = listOf(true, false, true, false, true, true)
        val p = mockk<Crawler.Page>()
        val wpc = mockk<WebPageCrawler>().also { crawler.mWebPageCrawler = it }
        val hs =
            mockk<ConcurrentHashMap.KeySetView<String, Boolean>>().also { crawler.mUniqueUris = it }
        val pc = mockk<Consumer<Crawler.Page>>()
        crawler.mMaxDepth = Int.MAX_VALUE
        mockkStatic(Stream::class)

        every { Stream.of<String>(any()) } answers { ss }
        every { ss.filter(any()) } answers {
            bl.forEachIndexed { i, b ->
                if (i < bl.lastIndex) {
                    assertThat(firstArg<Predicate<String>>().test("_")).isEqualTo(b)
                } else {
                    crawler.mMaxDepth = 2
                    assertThat(firstArg<Predicate<String>>().test("_")).isFalse
                }
            }
            ss
        }

        every { wpc.getPage(any()) } answers { p }
        every { crawler.callInManagedBlocker<Crawler.Page>(any()) } answers {
            assertThat(firstArg<Supplier<Crawler.Page>>().get()).isSameAs(p)
            p
        }
        every { hs.add(any()) } returnsMany bl
        every { pc.accept(any()) } just Runs
        every { ss.mapMulti<Crawler.Page>(any()) } answers {
            firstArg<BiConsumer<String, Consumer<Crawler.Page>>>().accept("", pc)
            ps
        }

        every { ps.mapMulti<Image>(any()) } answers {
            firstArg<BiConsumer<Crawler.Page, Consumer<Stream<Image>>>>().accept(mockk(), mockk())
            si
        }


        every { crawler.processPage(any(), any()) } answers { si }
        every { si.forEach(any()) } just Runs

        assertThat(crawler.crawlPage("", 3)).isSameAs(si)

        verify(exactly = 1) {
            Stream.of<String>(any())
            ss.filter(any())
            wpc.getPage(any())
            crawler.callInManagedBlocker<Crawler.Page>(any())
            pc.accept(any())
            ss.mapMulti<Crawler.Page>(any())
            ps.mapMulti<Image>(any())
            crawler.processPage(any(), any())
            si.forEach(any())
            crawler.crawlPage(any(), any())
            crawler.log(any(), *anyVararg())
        }
        verify(exactly = bl.size - 1) {
            hs.add(any())
        }
        confirmVerified(crawler, ps, si, ss, p, wpc, hs, pc)
    }

    @Test
    fun `processPage() uses expected chained method calls and lambdas`() {
        val p = mockk<Crawler.Page>()
        val d = 99
        val si = mockk<Stream<Image>>()
        val e = mockk<WebPageElement>()
        val mi = mockk<Stream<Image>>()
        val spe = mockk<Stream<WebPageElement>>()
        val u = mockk<URL>()
        val su = "mock"
        mockkStatic(Stream::class)
        every { p.getPageElementsAsStream(any(), any()) } answers {
            firstArg<Array<*>>().let {
                assertThat(it[0]).isNotEqualTo(it[1])
            }
            spe
        }
        every { spe.parallel() } answers { spe }

        every { e.type } returnsMany listOf(Crawler.Type.PAGE, Crawler.Type.IMAGE)
        every { e.getUrl() } answers { su }
        every { e.getURL() } answers { u }
        every { crawler.crawlPage(su, any()) } answers { mi }
        every { crawler.processImage(u) } answers { mi }
        every { mi.forEach(any()) } answers {
            println("forEach called")
        }
        every { spe.mapMulti<Image>(any()) } answers {
            firstArg<BiConsumer<WebPageElement, Consumer<Image>>>().accept(e, mockk())
            firstArg<BiConsumer<WebPageElement, Consumer<Image>>>().accept(e, mockk())
            si
        }
        assertThat(crawler.processPage(p, d)).isEqualTo(si)

        verify(exactly = 1) {
            p.getPageElementsAsStream(any(), any())
            spe.parallel()
            e.getUrl()
            crawler.processPage(any(), any())
            e.getURL()
            crawler.crawlPage(su, any())
            crawler.processImage(u)
            spe.mapMulti<Image>(any())
        }
        verify(exactly = 2) {
            mi.forEach(any())
            e.type
        }
        confirmVerified(crawler, p, si, e, mi, spe, u)
    }

    @Test
    fun `processImage() uses expected chained method calls`() {
        mockkStatic(Stream::class)
        val us = mockk<Stream<URL>>()
        val si = mockk<Stream<Image>>()
        val u = mockk<URL>()
        val i = mockk<Image>()
        mockkStatic(Stream::class)

        every { Stream.of(u) } answers { us }
        every { crawler.managedBlockerDownloadImage(any()) } answers {
            mockk()
        }
        every { si.forEach(any()) } just Runs
        every { us.mapMulti<Image>(any()) } answers {
            firstArg<BiConsumer<URL, Consumer<Stream<Image>>>>().accept(mockk(), mockk())
            si
        }
        every { crawler.getOrDownloadImage(any(), any()) } answers {
            secondArg<Consumer<Cache.Item>>().accept(mockk())
            i
        }
        every { crawler.transformImage(any()) } answers { si }

        assertThat(crawler.processImage(u)).isSameAs(si)

        verify(exactly = 1) {
            crawler.getOrDownloadImage(any(), any())
            crawler.processImage(u)
            us.mapMulti<Image>(any())
            crawler.transformImage(any())
            Stream.of(u)
            si.forEach(any())
            crawler.managedBlockerDownloadImage(any())
        }
        confirmVerified(crawler, us, si, u, i)
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

    @Test
    fun `transformImage() uses expected chained method calls`() {
        val lt = mockk<List<Transform>>()
        val si = mockk<Stream<Image>>()
        val st = mockk<Stream<Transform>>()
        val i = mockk<Image>()

        crawler.mTransforms = lt

        every { crawler.applyTransform(any(), any()) } answers { mockk() }
        every { lt.parallelStream() } returns st
        every { crawler.createNewCacheItem(any<Image>(), any()) } answers { mockk() }
        every { st.filter(any()) } answers {
            firstArg<Predicate<Transform>>().test(mockk())
            st
        }
        val ci = mockk<Consumer<Image>>()
        every { ci.accept(any()) } just Runs
        every { st.mapMulti<Image>(any()) } answers {
            firstArg<BiConsumer<Transform, Consumer<Image>>>().accept(mockk(), ci)
            si
        }

        assertThat(crawler.transformImage(i)).isEqualTo(si)

        verify(exactly = 1) {
            crawler.applyTransform(any(), any())
            lt.parallelStream()
            crawler.createNewCacheItem(any<Image>(), any())
            st.filter(any())
            ci.accept(any())
            st.mapMulti<Image>(any())
            crawler.transformImage(any())
        }
        confirmVerified(crawler, lt, si, st, i, ci)
    }
}
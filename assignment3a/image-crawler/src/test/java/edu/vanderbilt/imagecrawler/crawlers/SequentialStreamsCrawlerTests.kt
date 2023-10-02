package edu.vanderbilt.imagecrawler.crawlers

import admin.AssignmentTests
import edu.vanderbilt.imagecrawler.crawlers.Crawler.Type.IMAGE
import edu.vanderbilt.imagecrawler.crawlers.Crawler.Type.PAGE
import edu.vanderbilt.imagecrawler.platform.Cache
import edu.vanderbilt.imagecrawler.transforms.Transform
import edu.vanderbilt.imagecrawler.utils.Image
import edu.vanderbilt.imagecrawler.web.WebPageCrawler
import edu.vanderbilt.imagecrawler.web.WebPageElement
import io.mockk.MockKAnnotations
import io.mockk.confirmVerified
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.impl.annotations.SpyK
import io.mockk.mockk
import io.mockk.mockkStatic
import io.mockk.verify
import org.assertj.core.api.Assertions.assertThat
import org.junit.Before
import org.junit.Test
import java.net.URL
import java.util.concurrent.ConcurrentHashMap
import java.util.function.Consumer
import java.util.function.Function
import java.util.function.Predicate
import java.util.stream.Stream

class SequentialStreamsCrawlerTests : AssignmentTests() {
    @SpyK
    var crawler = SequentialStreamsCrawler()

    @MockK
    lateinit var uris: ConcurrentHashMap.KeySetView<String, Boolean>

    @MockK
    lateinit var image: Image

    private val esi = mockk<Stream<Image>>()

    @Before
    fun before() {
        MockKAnnotations.init(this, relaxUnitFun = true)
        every { crawler.log(any(), *anyVararg()) } answers { }
    }

    @Test
    fun `performCrawl() uses expected chained method calls`() {
        mockkStatic(Stream::class)
        val ss = mockk<Stream<String>>()
        val si = mockk<Stream<Image>>()
        every { crawler.crawlPage(any(), any()) } answers { si }
        every { ss.flatMap<Image>(any()) } answers {
            firstArg<Function<String, Stream<Image>>>().apply(".")
            si
        }
        every { si.count() } answers { -91 }
        every { Stream.of<String>(any()) } answers { ss }
        assertThat(crawler.performCrawl("", -99)).isEqualTo(-91)

        verify(exactly = 1) {
            crawler.crawlPage(any(), any())
            si.count()
            crawler.log(any(), *anyVararg())
            crawler.performCrawl(any(), any())
            ss.flatMap<Image>(any())
            Stream.of<String>(any())
        }
        confirmVerified(ss, si, crawler)
    }

    @Test
    fun `crawlPage() uses expected chained method calls and lambdas`() {
        val ps = mockk<Stream<Crawler.Page>>()
        val si = mockk<Stream<Image>>()
        val ss = mockk<Stream<String>>()
        val bl = listOf(true, false, true, false, true, true)
        val mp = mockk<Crawler.Page>()
        val wpc = mockk<WebPageCrawler>().also { crawler.mWebPageCrawler = it }
        val hs = mockk<ConcurrentHashMap.KeySetView<String, Boolean>>()
            .also { crawler.mUniqueUris = it }
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
        every { wpc.getPage(any()) } answers { mp }
        every { ss.map<Crawler.Page>(any()) } answers {
            firstArg<Function<String, Crawler.Page>>().apply("")
            ps
        }
        every { hs.add(any()) } returnsMany bl
        every { ps.filter(any()) } answers {
            listOf(mockk<Crawler.Page>(), null, mockk()).forEach {
                assertThat(firstArg<Predicate<Crawler.Page?>>().test(it)).isEqualTo(it != null)
            }
            ps
        }
        every { ps.flatMap<Image>(any()) } answers {
            firstArg<Function<Crawler.Page, Stream<Image>>>().apply(mockk())
            si
        }
        every { crawler.processPage(any(), any()) } answers { si }
        assertThat(crawler.crawlPage("", 3)).isSameAs(si)
        verify(exactly = 1) {
            Stream.of<String>(any())
            ss.filter(any())
            crawler.crawlPage(any(), any())
            wpc.getPage(any())
            ss.map<Crawler.Page>(any())
            ps.filter(any())
            ps.flatMap<Image>(any())
            crawler.processPage(any(), any())
            crawler.log(any(), *anyVararg())
        }
        verify(exactly = bl.size - 1) {
            hs.add(any())
        }
        confirmVerified(crawler, si, mp, ps, hs, ss, wpc)
    }

    @Test
    fun `processPage() uses expected chained method calls and lambdas`() {
        val i = mockk<Crawler.Page>()
        val d = 999
        val si = mockk<Stream<Image>>()
        val e1 = mockk<WebPageElement>()
        val mi = mockk<Stream<Image>>()
        val e2 = mockk<WebPageElement>()
        val spe = mockk<Stream<WebPageElement>>()
        val u = mockk<URL>()
        val s = "mock"
        every { e1.getUrl() } answers { s }
        every { e1.getURL() } answers { u }
        every { crawler.processImage(any()) } answers { mi }
        every { e1.type } answers { IMAGE }
        every { e2.getURL() } answers { u }
        every { e2.getUrl() } answers { s }
        every { e2.type } answers { PAGE }
        every { crawler.crawlPage(any(), any()) } answers { mi }
        every { i.getPageElementsAsStream(any(), any()) } answers {
            firstArg<Array<*>>().let {
                assertThat(it[0]).isNotEqualTo(it[1])
            }
            spe
        }
        every { spe.flatMap<Image>(any()) } answers {
            firstArg<Function<WebPageElement, Image>>().apply(e1)
            firstArg<Function<WebPageElement, Image>>().apply(e2)
            si
        }

        assertThat(crawler.processPage(i, d)).isEqualTo(si)

        verify(exactly = 1) {
            crawler.processImage(any())
            spe.flatMap<Image>(any())
            e1.getURL()
            e1.type
            crawler.processPage(i, d)
            e2.getUrl()
            e2.type
            crawler.crawlPage(any(), any())
            i.getPageElementsAsStream(any(), any())
        }

        confirmVerified(crawler, i, e1, e2, si, mi, spe)
    }

    @Test
    fun `processImage() uses expected chained method calls`() {
        mockkStatic(Stream::class)
        val us = mockk<Stream<URL>>()
        val si = mockk<Stream<Image>>()
        val i = mockk<Image>()
        val u = mockk<URL>()

        every { crawler.getOrDownloadImage(any(), any()) } answers {
            secondArg<Consumer<Cache.Item>>().accept(mockk())
            mockk()
        }
        every { us.map<Image>(any()) } answers {
            firstArg<Function<URL, Image>>().apply(mockk())
            si
        }
        every { crawler.downloadImage(any()) } returns i
        every { Stream.of(u) } answers { us }
        every { si.flatMap<Image>(any()) } answers {
            assertThat(firstArg<Function<Image, Image>>().apply(mockk())).isSameAs(si)
            si
        }
        every { crawler.transformImage(any()) } answers { si }
        every { si.filter(any()) } answers {
            si
        }

        assertThat(crawler.processImage(u)).isSameAs(si)

        verify(exactly = 1) {
            si.flatMap<Image>(any())
            crawler.transformImage(any())
            Stream.of(u)
            crawler.downloadImage(any())
            si.filter(any())
            crawler.getOrDownloadImage(any(), any())
            us.map<Image>(any())
        }
    }

    @Test
    fun `transformImage() uses expected chained method calls`() {
        val si = mockk<Stream<Image>>()
        val lt = mockk<List<Transform>>()
        val st = mockk<Stream<Transform>>()
        val i = mockk<Image>()

        crawler.mTransforms = lt

        every { st.map<Image>(any()) } answers {
            firstArg<Function<Transform, Image>>().apply(mockk())
            si
        }
        every { crawler.applyTransform(any(), any()) } answers { mockk() }
        every { si.filter(any()) } answers {
            assertThat(firstArg<Predicate<Image?>>().test(mockk())).isTrue
            assertThat(firstArg<Predicate<Image?>>().test(null)).isFalse
            si
        }
        every { lt.stream() } answers { st }
        every { crawler.createNewCacheItem(any<Image>(), any()) } answers { mockk() }
        every { st.filter(any()) } answers {
            firstArg<Predicate<Transform>>().test(mockk())
            st
        }

        assertThat(crawler.transformImage(i)).isEqualTo(si)

        verify(exactly = 1) {
            crawler.applyTransform(any(), any())
            si.filter(any())
            st.filter(any())
            crawler.createNewCacheItem(any<Image>(), any())
            lt.stream()
            st.map<Image>(any())
        }
    }
}
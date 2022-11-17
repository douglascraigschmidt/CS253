package edu.vanderbilt.imagecrawler.crawlers

import admin.AssignmentTests
import admin.injectInto
import edu.vanderbilt.imagecrawler.crawlers.RxFlowableCrawler.mapNotNull
import edu.vanderbilt.imagecrawler.platform.Cache
import edu.vanderbilt.imagecrawler.utils.Image
import edu.vanderbilt.imagecrawler.web.WebPageCrawler
import io.mockk.*
import io.mockk.impl.annotations.MockK
import io.mockk.impl.annotations.SpyK
import io.reactivex.rxjava3.core.Flowable
import io.reactivex.rxjava3.core.Single
import io.reactivex.rxjava3.functions.Function
import io.reactivex.rxjava3.functions.Predicate
import io.reactivex.rxjava3.parallel.ParallelFlowable
import io.reactivex.rxjava3.parallel.ParallelTransformer
import io.reactivex.rxjava3.schedulers.Schedulers
import org.assertj.core.api.Assertions.assertThat
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.reactivestreams.Publisher
import java.net.URL
import java.util.concurrent.ConcurrentHashMap
import java.util.function.Consumer
import kotlin.test.assertNotNull

open class RxFlowableCrawlerTests : AssignmentTests() {

    @SpyK
    var hs = ConcurrentHashMap.newKeySet<String>()

    @SpyK
    var crawler: RxFlowableCrawler = RxFlowableCrawler()

    @MockK
    lateinit var mi: Image

    @MockK
    lateinit var fi: Flowable<Image>

    @MockK
    lateinit var ls: Single<Long>

    @MockK
    lateinit var pf: Flowable<Crawler.Page>

    @MockK
    lateinit var cp: Crawler.Page

    @MockK
    lateinit var fs: Flowable<String>

    private val trampoline = Schedulers.trampoline()

    @Before
    open fun before() {
        mockkStatic(Schedulers::class)
        every { Schedulers.io() } returns trampoline
    }

    @After
    fun after() {
        clearAllMocks()
    }

    @Test
    fun `performCrawl uses the expected chained method calls and lambdas`() {
        every { ls.onErrorReturnItem(any()) } returns ls
        every { ls.blockingGet() } returns 1
        every { crawler.crawlPageAsync(any(), any()) } returns fi
        every { fi.count() } returns ls

        crawler.performCrawl("test", 3)

        verify(exactly = 1) {
            ls.onErrorReturnItem(0L)
            fi.count()
            crawler.crawlPageAsync(any(), 3)
            ls.blockingGet()
        }
    }

    @Test
    fun `crawlPageAsync uses the expected chained method calls and lambdas`() {
        mockkStatic(Flowable::class)
        crawler.mMaxDepth = Int.MAX_VALUE
        crawler.mUniqueUris = hs
        val pc = mockk<WebPageCrawler>()
        val bl = listOf(true, false, true, false, true, true)
        pc.injectInto(crawler)
        every { pf.flatMap(any<Function<Crawler.Page, Publisher<Image>>>()) } answers {
            firstArg<Function<Crawler.Page, Publisher<Image>>>().apply(mockk())
            fi
        }
        every { fs.map<Crawler.Page>(any()) } answers { pf }
        every { fs.filter(any()) } answers {
            bl.forEachIndexed { i, b ->
                if (i < bl.lastIndex) {
                    assertThat(firstArg<Predicate<String>>().test("_")).isEqualTo(b)
                } else {
                    crawler.mMaxDepth = 2
                    assertThat(firstArg<Predicate<String>>().test("_")).isFalse
                }
            }
            fs
        }
        every { Flowable.fromCallable<String>(any()) } returns fs
        every { hs.add(any()) } returnsMany bl
        every { crawler.imagesOnPageAndPageLinksAsync(any(), any()) } answers { fi }

        assertThat(crawler.crawlPageAsync("_", 3)).isSameAs(fi)

        verify(exactly = 1) {
            fs.filter(any())
            crawler.imagesOnPageAndPageLinksAsync(any(), any())
            Flowable.fromCallable<String>(any())
            pf.flatMap(any<Function<Crawler.Page, Publisher<Image>>>())
            fs.map<Crawler.Page>(any())
            crawler.crawlPageAsync(any(), any())
        }
        verify(exactly = bl.size - 1) {
            hs.add(any())
        }
        confirmVerified(crawler, fs, fi, hs)
    }

    @Test
    fun `imagesOnPageAndPageLinksAsync uses the expected chained method calls`() {
        every { fi.mergeWith(fi) } returns fi
        every { crawler.imagesOnPageLinksAsync(any(), any()) } returns fi
        every { crawler.imagesOnPageAsync(any()) } returns fi
        assertNotNull(crawler.imagesOnPageAndPageLinksAsync(cp, 0))
        verify {
            fi.mergeWith(fi)
            crawler.imagesOnPageAsync(cp)
            crawler.imagesOnPageAndPageLinksAsync(cp, 0)
            crawler.imagesOnPageLinksAsync(cp, 0)
        }
        confirmVerified(crawler, fi)
    }

    @Test
    fun `imagesOnPageAndPageLinksAsync uses the expected inner chained method calls`() {
        every { crawler.imagesOnPageAsync(any()) } returns Flowable.just(mi)
        every { crawler.imagesOnPageLinksAsync(any(), any()) } returns Flowable.just(mi)
        assertNotNull(crawler.imagesOnPageAndPageLinksAsync(cp, 3))
        verify {
            crawler.imagesOnPageAndPageLinksAsync(cp, 3)
            crawler.imagesOnPageAsync(cp)
            crawler.imagesOnPageLinksAsync(cp, 3)
        }
        confirmVerified(crawler, mi)
    }

    @Test
    fun `imagesOnPageLinksAsync uses the expected chained method calls and lambdas`() {
        mockkStatic(Flowable::class)
        val fs = mockk<Flowable<String>>()
        val pfs = mockk<ParallelFlowable<String>>()
        val pfi = mockk<ParallelFlowable<Image>>()
        val fi = mockk<Flowable<Image>>()
        val e = mockk<Flowable<Image>>()
        every { Flowable.fromIterable<String>(any()) } answers { fs }
        every { fs.parallel() } answers { pfs }
        every { pfs.runOn(any()) } answers {
            assertThat(firstArg<Any>()).isSameAs(Schedulers.trampoline())
            pfs
        }
        every { pfs.flatMap(any<Function<String, Publisher<Image>>>()) } answers {
            firstArg<Function<String, Publisher<Image>>>().apply("_")
            pfi
        }
        every { pfi.sequential() } answers { e }
        every { crawler.crawlPageAsync(any(), any()) } answers { fi }

        assertThat(crawler.imagesOnPageLinksAsync(mockk(), 99)).isSameAs(e)

        verify(exactly = 1) {
            Flowable.fromIterable<String>(any())
            fs.parallel()
            crawler.imagesOnPageLinksAsync(any(), any())
            pfs.runOn(any())
            pfs.flatMap(any<Function<String, Publisher<Image>>>())
            pfi.sequential()
            crawler.crawlPageAsync("_", any())
        }
        confirmVerified(crawler, fs, pfs, pfs, pfi)
    }

    @Test
    fun `imagesOnPageAsync uses the expected outer method calls and lambdas`() {
        mockkStatic(Flowable::class)
        mockkStatic(RxFlowableCrawler::class)
        val e = mockk<Flowable<Image>>()
        val fi = mockk<ParallelFlowable<Image>>()
        val uf = mockk<Flowable<URL>>()
        val pf = mockk<ParallelFlowable<URL>>()
        val mlu = mockk<MutableList<URL>>()
        val pta = mockk<ParallelTransformer<URL, Image>>()
        val ti = mockk<Flowable<Image>>()
        val ci = mockk<Cache.Item>()
        val u = mockk<URL>()
        val i = mockk<Image>()

        every { Flowable.fromIterable<URL>(any()) } answers { uf }
        every { uf.parallel() } answers { pf }
        every { pf.compose(any<ParallelTransformer<URL, Image>>()) } answers {
            fi
        }
        every { crawler.getOrDownloadImage(any(), any<Consumer<Cache.Item>>()) } answers {
            secondArg<Consumer<Cache.Item>>().accept(ci)
            mi
        }
        every { mapNotNull(any<Function<URL, Image>>()) } answers {
            assertThat(firstArg<Function<URL, Image>>().apply(u)).isSameAs(mi)
            pta
        }
        every { fi.flatMap<Image>(any()) } answers {
            firstArg<Function<Image, Publisher<Image>>>().apply(i)
            fi
        }
        every { crawler.transformImageAsync(any()) } answers { ti }
        every { pf.runOn(any()) } answers {
            assertThat(firstArg<Any>()).isSameAs(Schedulers.trampoline())
            pf
        }
        every { fi.sequential() } answers { e }
        every { cp.getPageElementsAsUrls(*anyVararg()) } answers {
            assertThat(args.size).isEqualTo(1)
            assertThat(firstArg<Array<Crawler.Type>>()).hasSize(1)
            assertThat(firstArg<Array<Crawler.Type>>()[0])
                .isNotEqualTo(Crawler.Type.PAGE)
            mlu
        }

        every { crawler.downloadImage(any()) } answers { i }

        assertThat(crawler.imagesOnPageAsync(cp)).isSameAs(e)

        verify(exactly = 1) {
            uf.parallel()
            crawler.getOrDownloadImage(any(), any<Consumer<Cache.Item>>())
            crawler.downloadImage(any())
            fi.flatMap<Image>(any())
            crawler.transformImageAsync(any())
            pf.runOn(any())
            cp.getPageElementsAsUrls(*anyVararg())
            mapNotNull(any<Function<URL, Image>>())
            Flowable.fromIterable<URL>(any())
            crawler.imagesOnPageAsync(cp)
            fi.sequential()
            pf.compose(any<ParallelTransformer<URL, Image>>())
        }
        confirmVerified(crawler, pf, uf, ti, ci, u, i, fi, mlu, pta, e)
    }
}
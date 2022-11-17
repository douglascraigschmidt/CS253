package edu.vanderbilt.imagecrawler.crawlers

import admin.AssignmentTests
import admin.injectInto
import edu.vanderbilt.imagecrawler.crawlers.RxObservableCrawler.mapNotNull
import edu.vanderbilt.imagecrawler.utils.Image
import edu.vanderbilt.imagecrawler.web.WebPageCrawler
import io.mockk.*
import io.mockk.impl.annotations.MockK
import io.mockk.impl.annotations.SpyK
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.core.ObservableTransformer
import io.reactivex.rxjava3.core.Single
import io.reactivex.rxjava3.schedulers.Schedulers
import org.assertj.core.api.Assertions.assertThat
import org.junit.After
import org.junit.Before
import org.junit.Test
import java.net.URL
import java.util.concurrent.ConcurrentHashMap
import kotlin.test.assertNotNull

open class RxObservableCrawlerTests : AssignmentTests() {

    @SpyK
    var spyCrawler: RxObservableCrawler = RxObservableCrawler()

    @MockK
    lateinit var mockImage: Image

    @MockK
    lateinit var mockImageObservable: Observable<Image>

    @MockK
    lateinit var mockLongSingle: Single<Long>

    @MockK
    lateinit var mockStringArray: List<String>

    @MockK
    lateinit var mockCrawlerPageObservable: Observable<Crawler.Page>

    @MockK
    lateinit var mockUrlObservable: Observable<URL>

    @MockK
    lateinit var mockPage: Crawler.Page

    @MockK
    lateinit var mockStringObservable: Observable<String>

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
    fun performCrawl() {
        every { mockImageObservable.count() } returns mockLongSingle
        every { mockLongSingle.blockingGet() } returns 1
        every { mockLongSingle.onErrorReturnItem(any()) } returns mockLongSingle
        every { spyCrawler.crawlPageAsync(any(), any()) } returns mockImageObservable

        // SUT
        spyCrawler.performCrawl("test", 3)

        verify { mockImageObservable.count() }
        verify { spyCrawler.crawlPageAsync(any(), 3) }
        verify { mockLongSingle.blockingGet() }
        verify { mockLongSingle.onErrorReturnItem(0L) }
    }

    @Test
    fun `crawlPageAsync uses the expected RxJava outer chained method calls`() {
        mockkStatic(Observable::class)
        val mockWebPageCrawler = mockk<WebPageCrawler>()
        mockWebPageCrawler.injectInto(spyCrawler)
        every { mockCrawlerPageObservable.flatMap<Image>(any()) } returns mockImageObservable
        every { mockStringObservable.map<Crawler.Page>(any()) } returns mockCrawlerPageObservable
        every { mockStringObservable.filter(any()) } returns mockStringObservable
        every { Observable.fromCallable<String>(any()) } returns mockStringObservable

        // SUT
        spyCrawler.crawlPageAsync("asdf", 3).blockingForEach {}

        verify { mockCrawlerPageObservable.flatMap<Image>(any()) }
        verify { mockStringObservable.filter(any()) }
        verify { Observable.fromCallable<String>(any()) }
        verify { mockStringObservable.map<Crawler.Page>(any()) }
    }

    @Test
    fun `crawlPageAsync uses the expected RxJava inner lambda calls`() {
        val mockWebPageCrawler = mockk<WebPageCrawler>()
        spyCrawler.mMaxDepth = Int.MAX_VALUE
        mockWebPageCrawler.injectInto(spyCrawler)

        // @@ Monte, can you please fix this?!
        val mockHashSet = spyk(ConcurrentHashMap.newKeySet<String>())
        mockHashSet.injectInto(spyCrawler)
        every { mockHashSet.add(any()) } returns true
        every { mockWebPageCrawler.getPage(any()) } returns mockPage

        // SUT
        spyCrawler.crawlPageAsync("asdf", 3).blockingForEach {}

        verify {
            mockHashSet.add(any())
            mockWebPageCrawler.getPage(any())
            spyCrawler.imagesOnPageLinksAsync(mockPage, 3)
        }
    }

    @Test
    fun `imagesOnPageAndPageLinksAsync uses the expected RxJava outer chained method calls`() {
        every { mockImageObservable.mergeWith(mockImageObservable) } returns mockImageObservable
        every { spyCrawler.imagesOnPageLinksAsync(any(), any()) } returns mockImageObservable
        every { spyCrawler.imagesOnPageAsync(any()) } returns mockImageObservable

        // SUT
        assertNotNull(spyCrawler.processPageAsync(mockPage, 0))

        verify { mockImageObservable.mergeWith(mockImageObservable) }
        verify { spyCrawler.imagesOnPageAsync(mockPage) }
        verify { spyCrawler.imagesOnPageLinksAsync(mockPage, 0) }
    }

    @Test
    fun `imagesOnPageAndPageLinksAsync uses the expected RxJava inner chained method calls`() {
        every { spyCrawler.imagesOnPageAsync(any()) } returns Observable.just(mockImage)
        every { spyCrawler.imagesOnPageLinksAsync(any(), any()) } returns Observable.just(mockImage)

        // SUT
        assertNotNull(spyCrawler.processPageAsync(mockPage, 3))

        verify { spyCrawler.imagesOnPageAsync(mockPage) }
        verify { spyCrawler.imagesOnPageLinksAsync(mockPage, 3) }
    }

    @Test
    fun `imagesOnPageLinksAsync uses the expected RxJava outer chained method calls`() {
        mockkStatic(Observable::class)
        val spyImageObservable = spyk<Observable<Image>>()
        every { mockStringObservable.flatMap<Image>(any()) } returns spyImageObservable
        every { Observable.fromIterable<String>(any()) } returns mockStringObservable
        every { mockPage.getPageElementsAsStrings(Crawler.Type.PAGE) } returns mockStringArray

        // SUT
        assertNotNull(spyCrawler.imagesOnPageLinksAsync(mockPage, 3))

        verify(exactly = 1) { Observable.fromIterable<String>(any()) }
        verify(exactly = 1) { mockStringObservable.flatMap<Image>(any()) }
        verify(exactly = 1) { mockPage.getPageElementsAsStrings(Crawler.Type.PAGE) }
        verify(exactly = 0) { spyImageObservable.subscribeOn(any()) }
    }

    @Test
    fun `imagesOnPageLinksAsync uses the expected RxJava inner chained method calls`() {
        every {
            mockPage.getPageElementsAsStrings(any())
        } returns listOf("a", "b", "c")

        val o = mockk<Observable<Image>>()

        every {
            o.subscribeOn(any())
        } returns Observable.just(mockImage, mockImage, mockImage)

        every { spyCrawler.crawlPageAsync(any(), any()) } returns o

        // SUT
        assertNotNull(spyCrawler.imagesOnPageLinksAsync(mockPage, 3).blockingForEach { })

        verify(exactly = 1) { mockPage.getPageElementsAsStrings(any()) }
        verify(exactly = 3) { spyCrawler.crawlPageAsync(any(), 4) }
        verify(exactly = 3) { o.subscribeOn(trampoline) }
    }

    @Test
    fun `imagesOnPageAsync uses the expected RxJava outer chained method calls`() {
        mockkStatic(Observable::class)
        every { mockImageObservable.flatMap<Image>(any()) } returns mockImageObservable
        every { mockUrlObservable.flatMap<Image>(any()) } returns mockImageObservable
        every { Observable.fromIterable<URL>(any()) } returns mockUrlObservable

        // SUT
        spyCrawler.imagesOnPageAsync(mockPage)

        verify { mockUrlObservable.flatMap<Image>(any()) }
        verify { mockImageObservable.flatMap<Image>(any()) }
        verify { Observable.fromIterable<URL>(any()) }
        verify(exactly = 0) { mockImageObservable.subscribeOn(any()) }
    }

    @Test
    fun `imagesOnPageAsync uses the expected RxJava inner chained method calls`() {
        mockkStatic(Observable::class)

        val map = mapOf(
            URL("http://www.dummy.url-1") to mockImage,
            URL("http://www.dummy.url-2") to mockImage
        )
        every { mockPage.getPageElementsAsUrls(any()) } returns
                map.keys.toList()
        every { spyCrawler.downloadImageAsync(any()) } returns
                Observable.just(mockImage)
        every { spyCrawler.transformImageAsync(any()) } returns
                Observable.just(mockImage)

        // SUT
        spyCrawler.imagesOnPageAsync(mockPage).toList().blockingGet()

        verify(exactly = 1) { mockPage.getPageElementsAsUrls(any()) }
        verify(exactly = map.size) { spyCrawler.downloadImageAsync(any()) }
        verify(exactly = map.size) { spyCrawler.transformImageAsync(any()) }
    }

    @Test
    fun `downloadImageAsync uses the expected RxJava operators`() {
        mockkStatic(RxObservableCrawler::class)
        mockkStatic(Observable::class)
        val crawler = spyk(RxObservableCrawler())
        val ot = mockk<ObservableTransformer<Any, Any>>()

        val url = URL("http://www.dummy.url-1")
        every { Observable.fromCallable<URL>(any()) } answers { mockUrlObservable }
        every { mockUrlObservable.subscribeOn(any()) } answers { mockUrlObservable }
        every { mockUrlObservable.compose<Image>(any()) } answers { mockImageObservable }
        every { mapNotNull<Any, Any>(any()) } answers { ot }

        // SUT
        assertThat(crawler.downloadImageAsync(url))
            .isEqualTo(mockImageObservable)

        verify(exactly = 1) {
            mockUrlObservable.compose<Image>(any())
            mockUrlObservable.subscribeOn(any())
            Observable.fromCallable<URL>(any())
            mapNotNull<Any, Any>(any())
        }
        clearStaticMockk(RxObservableCrawler::class)
        clearStaticMockk(Observable::class)
    }
}
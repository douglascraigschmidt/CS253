package edu.vanderbilt.imagecrawler.crawlers

import admin.AssignmentTests
import edu.vanderbilt.imagecrawler.transforms.Transform
import edu.vanderbilt.imagecrawler.utils.Crawler
import edu.vanderbilt.imagecrawler.utils.Image
import io.mockk.MockKAnnotations
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.impl.annotations.SpyK
import io.mockk.mockkStatic
import io.mockk.verify
import io.reactivex.Observable
import io.reactivex.Single
import io.reactivex.schedulers.Schedulers
import org.junit.Before
import org.junit.Test
import java.net.URL
import java.util.*

class RxObservableCrawlerTests : AssignmentTests() {
    @SpyK
    var mockCrawler: RxObservableCrawler = RxObservableCrawler()

    @MockK
    lateinit var mockImageObservable: Observable<Image>

    @MockK
    lateinit var mockOptionalImageObservable: Observable<Optional<Image>>

    @MockK
    lateinit var mockTransformObservable: Observable<Transform>

    @MockK
    lateinit var mockImage: Image

    @MockK
    lateinit var mockLongSingle: Single<Long>

    @MockK
    lateinit var mockTransforms: List<Transform>

    @MockK
    lateinit var mockStringArray: CustomArray<String>

    @MockK
    lateinit var mockCrawlerPageObservable: Observable<Crawler.Page>

    @MockK
    lateinit var mockUrlObservable: Observable<URL>

    @MockK
    lateinit var mockPage: Crawler.Page

    @MockK
    lateinit var mockStringObservable: Observable<String>

    @Test
    fun performCrawl() {
        every { mockImageObservable.count() } returns mockLongSingle
        every { mockLongSingle.blockingGet() } returns 1
        every { mockLongSingle.onErrorReturnItem(any()) } returns mockLongSingle
        every { mockCrawler.crawlPageAsync(any(), any()) } returns mockImageObservable

        // SUT
        mockCrawler.performCrawl("test", 3)

        verify { mockImageObservable.count() }
        verify { mockCrawler.crawlPageAsync(any(), any()) }
        verify { mockLongSingle.blockingGet() }
        verify { mockLongSingle.onErrorReturnItem(0L) }
    }

    @Test
    fun `crawlPageAsync contains the expected RxJava chained method calls`() {
        mockkStatic(Observable::class)
        every { mockCrawlerPageObservable.flatMap<Image>(any()) } returns mockImageObservable
        every { mockStringObservable.filter(any()) } returns mockStringObservable
        every { mockStringObservable.map<Crawler.Page>(any()) } returns mockCrawlerPageObservable
        every { Observable.just<String>(any()) } returns mockStringObservable

        // SUT
        mockCrawler.crawlPageAsync("asdf", 3)

        verify { mockStringObservable.map<Crawler.Page>(any()) }
        verify { mockStringObservable.filter(any()) }
        verify { Observable.just<String>(any()) }
        verify { mockCrawlerPageObservable.flatMap<Image>(any()) }
    }

    @Test
    fun `imagesOnPageAsyncAndPageLinks contains the expected RxJava chained method calls`() {
        every { mockCrawler.imagesOnPageAsync(any()) } returns mockImageObservable
        every { mockImageObservable.subscribeOn(any()) } returns mockImageObservable
        every { mockImageObservable.mergeWith(mockImageObservable) } returns mockImageObservable
        every { mockCrawler.imagesOnPageAsyncLinks(any(), any()) } returns mockImageObservable

        // SUT
            mockCrawler.imagesOnPageAsyncAndPageLinks(mockPage, 0)

        verify { mockImageObservable.mergeWith(mockImageObservable) }
        verify { mockImageObservable.subscribeOn(Schedulers.io()) }
        verify { mockCrawler.imagesOnPageAsync(mockPage) }
        verify { mockCrawler.imagesOnPageAsyncLinks(mockPage, 0) }
    }

    @Test
    fun `imagesOnPageAsyncLinks contains the expected RxJava chained method calls`() {
        mockkStatic(Observable::class)
        every { mockStringObservable.flatMap<Image>(any()) } returns mockImageObservable
        every { mockPage.getPageElementsAsStrings(Crawler.Type.PAGE) } returns mockStringArray
        every { Observable.fromIterable<String>(any()) } returns mockStringObservable

        // SUT
        mockCrawler.imagesOnPageAsyncLinks(mockPage, 3)

        verify { mockStringObservable.flatMap<Image>(any()) }
        verify { Observable.fromIterable<String>(any()) }
        verify { mockPage.getPageElementsAsStrings(Crawler.Type.PAGE) }
    }

    @Test
    fun `imagesOnPageAsync contains the expected RxJava chained method calls`() {
        mockkStatic(Observable::class)
        every { mockOptionalImageObservable.map<Image>(any()) } returns mockImageObservable
        every { mockUrlObservable.map<Optional<Image>>(any()) } returns mockOptionalImageObservable
        every { mockOptionalImageObservable.filter(any()) } returns mockOptionalImageObservable
        every { mockImageObservable.flatMap<Image>(any()) } returns mockImageObservable
        every { mockCrawlerPageObservable.flatMap<URL>(any()) } returns mockUrlObservable
        every { Observable.just<Crawler.Page>(any()) } returns mockCrawlerPageObservable

        // SUT
        mockCrawler.imagesOnPageAsync(mockPage)

        every { mockOptionalImageObservable.filter(any()) }
        every { mockUrlObservable.map<Optional<Image>>(any()) }
        every { mockOptionalImageObservable.map<Image>(any()) }
        every { Observable.just<Crawler.Page>(any()) }
        every { mockImageObservable.flatMap<Image>(any()) }
        every { mockCrawlerPageObservable.flatMap<URL>(any()) }
    }


    @Test
    fun `transformImageParallel contains the expected RxJava chained method calls`() {
        mockkStatic(Observable::class)
        every { Observable.fromIterable<Transform>(any()) } returns mockTransformObservable
        every { mockTransformObservable.subscribeOn(any()) } returns mockTransformObservable
        every { mockTransformObservable.filter(any()) } returns mockTransformObservable
        every { mockTransformObservable.map<Optional<Image>>(any()) } returns mockOptionalImageObservable
        every { mockOptionalImageObservable.filter(any()) } returns mockOptionalImageObservable
        every { mockOptionalImageObservable.map<Image>(any()) } returns mockImageObservable

        // SUT
        mockCrawler.mTransforms = mockTransforms
        mockCrawler.transformImageAsync(mockImage)

        every { Observable.fromIterable(mockCrawler.mTransforms) }
        every { mockTransformObservable.subscribeOn(Schedulers.io()) }
        every { mockTransformObservable.filter(any()) }
        every { mockTransformObservable.map<Optional<Image>>(any()) }
        every { mockOptionalImageObservable.filter(any()) }
        every { mockOptionalImageObservable.map<Image>(any()) }
    }
}

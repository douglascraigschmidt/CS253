package edu.vanderbilt.imagecrawler.crawlers

import admin.AssignmentTests
import admin.getField
import edu.vanderbilt.imagecrawler.transforms.Transform
import edu.vanderbilt.imagecrawler.utils.Crawler
import edu.vanderbilt.imagecrawler.utils.Image
import edu.vanderbilt.imagecrawler.utils.UnsynchronizedArray
import edu.vanderbilt.imagecrawler.utils.WebPageCrawler
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.impl.annotations.SpyK
import io.mockk.mockk
import io.mockk.mockkStatic
import io.mockk.verify
import org.junit.Assert.*
import org.junit.Test
import java.lang.RuntimeException
import java.net.URL
import java.util.*
import java.util.concurrent.CompletableFuture
import java.util.concurrent.TimeUnit
import java.util.function.Function
import java.util.stream.Stream

class CompletableFuturesCrawlerTests : AssignmentTests(0) {
    @SpyK
    var mockCrawler = CompletableFuturesCrawler()

    @MockK
    lateinit var mockWebPageCrawler: WebPageCrawler

    @MockK
    lateinit var mockPageFuture: CompletableFuture<Crawler.Page>

    @MockK
    lateinit var mockURLArrayFuture: CompletableFuture<CustomArray<URL>>

    @MockK
    lateinit var mockIntFuture: CompletableFuture<Int>

    @MockK
    lateinit var mockPage: Crawler.Page

    @Test
    fun testMembersWhiteBox() {
        mockkStatic(CompletableFuture::class)
        val crawler = CompletableFuturesCrawler()
        val member: CompletableFuture<Int>? = crawler.getField("mZero")
        assertNotNull(member)

        verify(exactly = 1) { CompletableFuture.completedFuture(0) }
    }

    @Test
    fun performCrawlWhiteBox() {
        val uri = "https://www.no.where"
        val depth = Int.MAX_VALUE
        val imageCount = 10

        /******* TEST SETUP ************/

        every { mockCrawler.performCrawlAsync(any(), any()) } returns mockIntFuture
        every { mockIntFuture.join() } returns imageCount

        /******* TEST CALL ************/

        val result = mockCrawler.performCrawl(uri, depth)

        /******* TEST EVALUATION ************/

        assertEquals(imageCount, result)

        verify(exactly = 1) { mockCrawler.performCrawlAsync(uri, depth) }
        verify(exactly = 1) { mockIntFuture.join() }
    }

    @Test
    fun getPageAsyncWhiteBox() {
        val uri = "https://www.no.where"

        /******* TEST SETUP ************/

        mockCrawler.mWebPageCrawler = mockWebPageCrawler
        every { mockWebPageCrawler.getPage(any()) } returns mockPage

        mockkStatic(CompletableFuture::class)

        /******* TEST CALL ************/

        val resultFuture = mockCrawler.getPageAsync(uri)

        /******* TEST EVALUATION ************/

        assertNotNull(resultFuture)

        val page = resultFuture.get(10, TimeUnit.SECONDS)
        assertNotNull(page)
        assertSame(mockPage, page)

        verify(exactly = 1) { mockWebPageCrawler.getPage(uri) }
        verify(exactly = 1) { CompletableFuture.supplyAsync<Int>(any()) }
    }

    @Test
    fun getPageAsyncIsEfficientWhiteBox() {
        mockCrawler.mWebPageCrawler = mockWebPageCrawler
        mockkStatic(CompletableFuture::class)
        every { CompletableFuture.supplyAsync<Int>(any()) } returns mockIntFuture
        assertNotNull(mockCrawler.getPageAsync("https://www.no.where"))
        verify(exactly = 1) { CompletableFuture.supplyAsync<Int>(any()) }
        verify(exactly = 0) { (mockIntFuture.thenApply<Int>(any())) }
    }

    @Test
    fun getImagesOnPageAsyncWhiteBox() {

        /******* TEST SETUP ************/

        mockCrawler.mWebPageCrawler = mockWebPageCrawler

        every {
            mockPageFuture.thenApplyAsync(any<Function<Crawler.Page, CustomArray<URL>>>())
        } returns mockURLArrayFuture

        every {
            mockURLArrayFuture.thenComposeAsync(
                    any<Function<CustomArray<URL>, CompletableFuture<Int>>>())
        } returns mockIntFuture


        /******* TEST CALL ************/

        val result = mockCrawler.getImagesOnPageAsync(mockPageFuture)

        /******* TEST EVALUATION ************/

        assertNotNull(result)
        assertSame(mockIntFuture, result)

        verify(exactly = 1) {
            mockPageFuture.thenApplyAsync(any<Function<Crawler.Page, CustomArray<URL>>>())
        }

        verify(exactly = 1) {
            mockURLArrayFuture.thenComposeAsync(
                    any<Function<CustomArray<URL>, CompletableFuture<Int>>>())
        }
    }

    @Test
    fun getImagesOnPageAsyncBlackBox() {
        val expectedIntResult = 10

        /******* TEST SETUP ************/

        mockCrawler.mWebPageCrawler = mockWebPageCrawler

        val pageFuture = CompletableFuture.completedFuture(mockPage)

        every { mockCrawler.getImagesOnPage(any()) } answers {
            UnsynchronizedArray()
        }

        every {
            mockCrawler.processImages(any())
        } answers {
            val result = CompletableFuture<Int>()
            result.complete(expectedIntResult)
            result
        }

        /******* TEST CALL ************/

        val futureResult = mockCrawler.getImagesOnPageAsync(pageFuture)

        /******* TEST EVALUATION ************/

        assertNotNull(futureResult)

        val getResult = futureResult.get(100, TimeUnit.MILLISECONDS)
        assertEquals(expectedIntResult, getResult)

        verify(exactly = 1) { mockCrawler.getImagesOnPage(any()) }
        verify(exactly = 1) {
            mockCrawler.processImages(any<CustomArray<URL>>())
        }
    }

    @Test
    fun crawlHyperLinksOnPageAsyncWhiteBox() {
        val depth = Int.MAX_VALUE

        /******* TEST SETUP ************/

        every {
            mockPageFuture.thenComposeAsync(
                    any<Function<Crawler.Page, CompletableFuture<Int>>>())
        } returns mockIntFuture

        /******* TEST CALL ************/

        val result = mockCrawler.crawlHyperLinksOnPageAsync(mockPageFuture, depth)

        /******* TEST EVALUATION ************/

        assertNotNull(result)
        assertSame(result, mockIntFuture)

        verify(exactly = 1) {
            mockPageFuture.thenComposeAsync(
                    any<Function<Crawler.Page, CompletableFuture<Int>>>())
        }
    }

    @Test
    fun crawlHyperLinksOnPageAsyncBlackBox() {
        val depth = Int.MAX_VALUE
        val expectedIntResult = 10

        /******* TEST SETUP ************/

        val pageFuture = CompletableFuture.completedFuture(mockPage)

        every {
            mockCrawler.crawlHyperLinksOnPage(any(), any())
        } answers {
            val result = CompletableFuture<Int>()
            result.complete(expectedIntResult)
            result
        }

        /******* TEST CALL ************/

        val futureResult = mockCrawler.crawlHyperLinksOnPageAsync(pageFuture, depth)

        /******* TEST EVALUATION ************/

        assertNotNull(futureResult)

        val getResult = futureResult.get(100, TimeUnit.MILLISECONDS)
        assertEquals(expectedIntResult, getResult)

        verify(exactly = 1) { mockCrawler.crawlHyperLinksOnPage(any(), any()) }
    }

    @Test
    fun combineResultsBlackBox() {
        val random = Random()

        /******* TEST SETUP ************/

        val imagesOnPage = 10 + random.nextInt(10)
        val imagesOnPageLinks = 10 + random.nextInt(10)
        val pageFuture = CompletableFuture<Int>()
        pageFuture.complete(imagesOnPage)
        val pageLinksFuture = CompletableFuture<Int>()
        pageLinksFuture.complete(imagesOnPageLinks)

        /******* TEST CALL ************/

        val futureResult = mockCrawler.combineResults(pageFuture, pageLinksFuture)

        /******* TEST EVALUATION ************/

        assertNotNull(futureResult)

        val intResult = futureResult.get(100, TimeUnit.MILLISECONDS)

        assertEquals(imagesOnPage + imagesOnPageLinks, intResult)
    }

    @Test
    fun combineResultsWhiteBox() {
        val mockFuture1 = mockk<CompletableFuture<Int>>()
        val mockFuture2 = mockk<CompletableFuture<Int>>()
        val mockFuture3 = mockk<CompletableFuture<Int>>()

        every { mockFuture1.thenCombine<Int, Int>(any(), any()) } returns mockFuture3
        assertSame(mockFuture3, mockCrawler.combineResults(mockFuture1, mockFuture2))

        verify(exactly = 1) { mockFuture1.thenCombine<Int, Int>(mockFuture2, any()) }
    }

    @Test
    fun crawlHyperLinksOnPageBlackBox() {
        val depth = Int.MAX_VALUE
        val hyperLinkCount = 10

        /******* TEST SETUP ************/

        // Calculate number of images separately, not in thenAnswer() callback below
        // because this calculated value is an invariant that should not depend on
        // the user code.
        var expectedImages = 0
        for (i in 1..hyperLinkCount) {
            expectedImages += i
        }

        every {
            mockPage.getPageElementsAsStrings(Crawler.Type.PAGE)
        } answers {
            val array = UnsynchronizedArray<String>()
            for (i in 1..hyperLinkCount) {
                array.add("$i")
            }
            array
        }

        every {
            mockCrawler.performCrawlAsync(any(), any())
        } answers {
            val hyperLink = call.invocation.args[0] as String
            val intValue = hyperLink.toInt()
            val intFuture = CompletableFuture<Int>()
            intFuture.complete(intValue)
            intFuture
        }

        /******* TEST CALL ************/

        val futureResult = mockCrawler.crawlHyperLinksOnPage(mockPage, depth)

        /******* TEST EVALUATION ************/

        assertNotNull(futureResult)

        verify(exactly = hyperLinkCount) { mockCrawler.performCrawlAsync(any(), any()) }

        val intResult = futureResult.get(100, TimeUnit.MILLISECONDS)
        assertEquals(expectedImages, intResult)
    }

    @Test
    fun processImagesBlackBox() {
        val urlCount = 10
        val urls = UnsynchronizedArray<URL>()

        /******* TEST SETUP ************/

        for (i in 1..urlCount) {
            urls.add(URL("https://test.com/$i"))
        }

        val images = mutableListOf<Image>()
        for (i in 1..urlCount) {
            images.add(mockk())
        }

        val mockImage = mockk<Image>()
        val imageFuture = CompletableFuture<Image>()
        imageFuture.complete(mockImage)

        every {
            mockCrawler.downloadAndStoreImageAsync(any())
        } returns imageFuture

        every {
            mockCrawler.transformImageAsync(any<CompletableFuture<Image>>())
        } answers {
            val imageStreamFuture = CompletableFuture<Stream<Image>>()
            imageStreamFuture.complete(images.stream())
            imageStreamFuture
        }

        /******* TEST CALL ************/

        val futureResult = mockCrawler.processImages(urls)

        /******* TEST EVALUATION ************/

        assertNotNull(futureResult)

        verify(exactly = urlCount) { mockCrawler.downloadAndStoreImageAsync(any()) }

        verify(exactly = urlCount) {
            mockCrawler.transformImageAsync(any<CompletableFuture<Image>>())
        }

        val intResult = futureResult.get(100, TimeUnit.MILLISECONDS)
        assertEquals(urlCount * urlCount, intResult)
    }

    @MockK
    lateinit var mockImageStreamFuture: CompletableFuture<Stream<Image>>

    // Terminate stream and collect results into
    // a completable future to an array of Images
    // that indicate the success or failure of
    // the transform operation.
    @Test
    fun transformImageAsyncWhiteBox() {
        /******* TEST SETUP ************/

        val mockTransformStream = mockk<Stream<Transform>>()
        val mockImageStream = mockk<Stream<Image>>()
        val mockImage = mockk<Image>()
        val imageFuture = CompletableFuture<Image>()
        imageFuture.complete(mockImage)

        mockCrawler.mTransforms = mockk<List<Transform>>()
        every {
            mockImageStream.collect<CompletableFuture<Stream<Image>>, Any>(any())
        } returns mockImageStreamFuture
        every { mockTransformStream.map<Image>(any()) } returns mockImageStream
        every { mockTransformStream.filter(any()) } returns mockTransformStream
        every { mockCrawler.mTransforms.stream() } returns mockTransformStream


        /******* TEST CALL ************/

        assertNotNull(mockCrawler.transformImageAsync(imageFuture))

        /******* TEST EVALUATION ************/

        verify(exactly = 1) { mockTransformStream.filter(any()) }
        verify(exactly = 1) { mockImageStream.collect<CompletableFuture<Stream<Image>>, Any>(any()) }
        verify(exactly = 1) { mockTransformStream.map<Image>(any()) }
        verify(exactly = 1) { mockCrawler.mTransforms.stream() }
    }

    @Test
    fun transformImageAsyncBlackBox() {
        val random = Random()
        val transforms = 10 + random.nextInt(10)

        /******* TEST SETUP ************/

        val transformArray = mutableListOf<Boolean>()

        repeat(transforms) {
            transformArray.add(random.nextBoolean())
        }

        val imageCount = transformArray.filter { it }.count()

        mockCrawler.mTransforms = buildMockTransformList(transforms)

        val mockImage = mockk<Image>()
        val imageFuture = CompletableFuture<Image>()
        imageFuture.complete(mockImage)

        var index = 0
        every {
            mockCrawler.createNewCacheItem(any(), any())
        } answers {
            transformArray[index++]
        }

        every {
            mockCrawler.applyTransformAsync(any(), any())
        } answers {
            val transformedImageFuture = CompletableFuture<Image>()
            transformedImageFuture.complete(mockImage)
            transformedImageFuture
        }

        /******* TEST CALL ************/

        val futureResult = mockCrawler.transformImageAsync(imageFuture)

        /******* TEST EVALUATION ************/

        assertNotNull(futureResult)

        val imageStream = futureResult.get(100, TimeUnit.MILLISECONDS)

        val intResult = imageStream.count()
        assertEquals(imageCount.toLong(), intResult)

        verify(exactly = transforms) {
            mockCrawler.createNewCacheItem(any(), any())
        }

        verify(exactly = imageCount) {
            mockCrawler.applyTransformAsync(any(), any())
        }
    }

    private fun buildMockTransformList(transforms: Int): MutableList<Transform> {
        val mockTransform = mockk<Transform>()
        val transformList = mutableListOf<Transform>()

        repeat(transforms) {
            transformList.add(mockTransform)
        }

        return transformList
    }
}

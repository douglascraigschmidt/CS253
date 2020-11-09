package edu.vanderbilt.imagecrawler.crawlers

import admin.AssignmentTests
import admin.setField
import edu.vanderbilt.imagecrawler.transforms.Transform
import edu.vanderbilt.imagecrawler.utils.*
import edu.vanderbilt.imagecrawler.utils.Crawler.Type.IMAGE
import edu.vanderbilt.imagecrawler.utils.Crawler.Type.PAGE
import io.mockk.*
import io.mockk.impl.annotations.MockK
import io.mockk.impl.annotations.SpyK
import org.junit.Before
import org.junit.Test
import java.net.URL
import java.util.*
import java.util.stream.IntStream
import java.util.stream.Stream
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals
import kotlin.test.assertSame

class SequentialStreamsCrawlerTests : AssignmentTests() {
    @SpyK
    var mockCrawler = SequentialStreamsCrawler()

    @MockK
    lateinit var mockElements: CustomArray<WebPageElement>

    @MockK
    lateinit var mockElementStream: Stream<WebPageElement>

    @MockK
    lateinit var mockIntStream: IntStream

    @MockK
    lateinit var mockUniqueUris: ConcurrentHashSet<String>

    @MockK
    lateinit var mockImageStream: Stream<Image>

    @MockK
    lateinit var mockTransforms: List<Transform>

    @MockK
    lateinit var mockWebPageCrawler: WebPageCrawler

    @MockK
    lateinit var mockPage: Crawler.Page

    @Before
    fun before() {
        MockKAnnotations.init(this, relaxUnitFun = true)
        every { mockCrawler.log(any()) } answers { Unit }
    }

    @Test
    fun crawPageTest1() {
        crawlPage(0, 10)
    }

    @Test
    fun crawPageTest2() {
        crawlPage(10, 0)
    }

    @Test
    fun crawPageTest3() {
        crawlPage(10, 10)
    }

    @Test
    fun crawPageWithFailuresTest4() {
        val random = Random()
        repeat(10) {
            crawlPage(10 + random.nextInt(90),
                    10 + random.nextInt(90),
                    failures = true)
            resetAll()
        }
    }

    @Test
    fun processImageTest5() {
        val random = Random()
        repeat(5) {
            processImageTest(random.nextInt(10), 0)
            resetAll()
        }
    }

    @Test
    fun transformImageTest6() {
        val random = Random()
        repeat(10) {
            processImageTest(random.nextInt(10), random.nextInt(10))
            resetAll()
        }
    }

    @Test
    fun `crawlPage() should use reasonable combination of chain method calls`() {
        val expected = -99
        val mockStreamInt = mockk<Stream<Int>>()
        val depth = Random().nextInt(134)

        every { mockCrawler.log(any()) } answers { Unit }
        every { mockWebPageCrawler.getPage(any()) } returns mockPage
        every { mockPage.getPageElements(any(), any()) } returns mockElements
        every { mockElements.stream() } returns mockElementStream
        every { mockElementStream.mapToInt(any()) } returns mockIntStream
        every { mockElementStream.map<Int>(any()) } returns mockStreamInt
        every { mockStreamInt.reduce(any(), any()) } returns expected
        every { mockIntStream.sum() } returns expected

        mockCrawler.mWebPageCrawler = mockWebPageCrawler
        mockCrawler.mMaxDepth = Int.MAX_VALUE
        mockCrawler.mUniqueUris = mockUniqueUris

        // SUT
        val actual = mockCrawler.crawlPage("mock-page", depth)

        verify(exactly = 1) { mockCrawler.crawlPage("mock-page", depth) }
        verify(exactly = 1) { mockWebPageCrawler.getPage(any()) }
        verify(exactly = 1) { mockPage.getPageElements(any(), any()) }
        verify(exactly = 1) { mockElements.stream() }

        verifyOneOf("Not using optimal chain call variations.",
                {
                    verifySequence {
                        mockElementStream.map<Int>(any())
                        mockStreamInt.reduce(any(), any())
                    }
                },
                {
                    verifySequence {
                        mockElementStream.mapToInt(any())
                        mockIntStream.sum()
                    }
                }
        )

        assertEquals(expected, actual)
    }

    @Test
    fun `crawlPage() should use correct chain call parameters`() {
        every { mockCrawler.log(any()) } answers { Unit }
        val mockImageElement = mockk<WebPageElement>()
        val mockPageElement = mockk<WebPageElement>()
        val mockPageURL = mockk<URL>()

        every { mockImageElement.type } returns IMAGE
        every { mockImageElement.getURL() } returns mockPageURL

        every { mockPageElement.type } returns PAGE
        every { mockPageElement.getUrl() } returns "mock.page.url"

        val elementStream: Stream<WebPageElement> =
                Stream.of(mockImageElement, mockPageElement)
        val random = Random()
        val depth = random.nextInt(134)
        val images = random.nextInt(492)
        val pages = random.nextInt(235)

        every { mockWebPageCrawler.getPage(any()) } returns mockPage
        every { mockPage.getPageElements(any(), any()) } returns mockElements
        every { mockElements.stream() } returns elementStream
        every { mockCrawler.performCrawl("mock.page.url", depth + 1) } returns pages
        every { mockCrawler.processImage(mockPageURL) } returns images

        mockCrawler.mWebPageCrawler = mockWebPageCrawler
        mockCrawler.mMaxDepth = Int.MAX_VALUE
        mockCrawler.mUniqueUris = mockUniqueUris

        // SUT
        val count = mockCrawler.crawlPage("mock-page", depth)

        /******* TEST EVALUATION ************/

        assertEquals(images + pages, count)

        verify(exactly = 1) { mockWebPageCrawler.getPage(any()) }
        verify(exactly = 1) { mockPage.getPageElements(any(), any()) }
        verify(exactly = 1) { mockElements.stream() }
        verify(exactly = 1) { mockCrawler.crawlPage("mock-page", depth) }
        verify(exactly = 1) { mockCrawler.performCrawl("mock.page.url", depth + 1) }
        verify(exactly = 1) { mockCrawler.processImage(mockPageURL) }
    }

    @Test
    fun `processImages() should use correct chain method calls`() {
        val mockImage = mockk<Image>()
        val mockTransformStream = mockk<Stream<Transform>>()
        mockCrawler.mTransforms = mockTransforms
        every { mockCrawler.getOrDownloadImage(any()) } returns mockImage
        every { mockTransforms.stream() } returns mockTransformStream
        every { mockTransformStream.filter(any()) } returns mockTransformStream
        every { mockTransformStream.count() } returns 99
        every { mockTransformStream.map(any<java.util.function.Function<Any, Any>>()) } throws
                (Exception("map should not be used in your solution!"))
        every { mockTransformStream.reduce(any(), any()) } throws
                (Exception("reduce should not be used in your solution!"))

        val count = mockCrawler.processImage(mockk())

        assertEquals(99, count)
        verify(exactly = 1) { mockCrawler.getOrDownloadImage(any()) }
        verify(exactly = 1) { mockTransforms.stream() }
        verify(exactly = 1) { mockTransformStream.filter(any()) }
        verify(exactly = 1) { mockTransformStream.count() }
    }

    private fun resetAll() {
        clearMocks(
                mockCrawler,
                mockImageStream,
                mockElements,
                mockTransforms,
                mockUniqueUris,
                mockPage,
                mockUniqueUris,
                mockWebPageCrawler
        )
        every { mockCrawler.log(any()) } answers { Unit }
    }

    private fun crawlPage(pages: Int, images: Int, failures: Boolean = false) {
        /******* TEST SETUP ************/
        val rootUrl = "/root"
        val imageRet = 1
        val startDepth = 777
        val maxDepth = Int.MAX_VALUE
        var processImageCount = 0
        var expected = 0

        val pageElements = (1..pages).map {
            WebPageElement("http://www/PAGE/$it", PAGE)
        }.shuffled()

        val imageElements = (1..images).map {
            WebPageElement("http://www/IMAGE/$it", IMAGE)
        }.shuffled()

        val elements = (pageElements + imageElements).shuffled()

        mockCrawler.setField("mWebPageCrawler", mockWebPageCrawler)
        assertSame(mockWebPageCrawler, mockCrawler.mWebPageCrawler)

        every { mockWebPageCrawler.getPage(any()) } returns mockPage
        every { mockPage.getPageElements(any(), any()) } answers {
            val args = call.invocation.args[0] as Array<*>
            assertNotEquals(args[0], args[1])
            mockElements
        }
        every { mockElements.stream() } returns elements.stream()

        if (images > 0) {
            every { mockCrawler.processImage(any()) } answers {
                ++processImageCount
                if (!failures || processImageCount.rem(2) == 0) {
                    expected++
                    1
                } else {
                    0
                }
            }
        }

        mockCrawler.mWebPageCrawler = mockWebPageCrawler
        mockCrawler.mMaxDepth = maxDepth
        mockCrawler.mUniqueUris = mockUniqueUris

        /******* TEST CALL ************/

        val count = mockCrawler.crawlPage(rootUrl, startDepth)

        /******* TEST EVALUATION ************/

        assertEquals(expected, count)

        verify(exactly = 1) { mockPage.getPageElements(any(), any()) }
        verify(exactly = 1) { mockWebPageCrawler.getPage(any()) }
        verify(exactly = images * imageRet) { mockCrawler.processImage(any()) }
        verify(exactly = pages) { mockCrawler.performCrawl(any(), any()) }
    }

    private fun processImageTest(transforms: Int, failures: Int) {
        val imageUrl = "http://www.mock.com/image"
        val url = URL(imageUrl)
        val mockImage = mockk<Image>()
        val mockTransform = mockk<Transform>()
        var transformCount = 0

        val transformList = mutableListOf<Transform>()
        repeat(transforms + failures) {
            transformList.add(mockTransform)
        }

        mockCrawler.mTransforms = mockTransforms

        every { mockTransforms.stream() } returns transformList.stream()
        every { mockCrawler.getOrDownloadImage(url) } returns mockImage
        every { mockCrawler.createNewCacheItem(any(), any()) } returns true
        every { mockCrawler.applyTransform(any(), any()) } answers {
            if (++transformCount <= transforms) {
                mockImage
            } else {
                null
            }
        }

        /******* TEST CALL ************/

        val count = mockCrawler.processImage(url)

        /******* TEST EVALUATION ************/

        assertEquals(transforms, count)

        verify(exactly = 1) { mockTransforms.stream() }
        verify(exactly = 1) { mockCrawler.getOrDownloadImage(url) }
        verify(exactly = transformList.size) { mockCrawler.createNewCacheItem(any(), any()) }
        verify(exactly = transformList.size) { mockCrawler.applyTransform(any(), any()) }
    }
}
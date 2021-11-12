package edu.vanderbilt.imagecrawler.crawlers

import admin.AssignmentTests
import edu.vanderbilt.imagecrawler.utils.*
import edu.vanderbilt.imagecrawler.utils.Crawler.Type.IMAGE
import edu.vanderbilt.imagecrawler.utils.Crawler.Type.PAGE
import io.mockk.*
import io.mockk.impl.annotations.MockK
import io.mockk.impl.annotations.SpyK
import org.assertj.core.api.Assertions.assertThat
import org.junit.Before
import org.junit.Test
import java.net.URL
import java.util.*
import java.util.stream.IntStream
import java.util.stream.Stream

class SequentialStreamsCrawlerTests : AssignmentTests() {
    @SpyK
    var mockCrawler = SequentialStreamsCrawler()

    @MockK
    lateinit var mockElements: List<WebPageElement>

    @MockK
    lateinit var mockUniqueUris: ConcurrentHashSet<String>

    @MockK
    lateinit var mockImage: Image

    @MockK
    lateinit var mockPageElementStream: Stream<WebPageElement>

    private val expected = -99

    @Before
    fun before() {
        MockKAnnotations.init(this, relaxUnitFun = true)
        every { mockCrawler.log(any(), *anyVararg()) } answers { }
        every { mockCrawler.runLocalTransforms() } returns true
        every { mockCrawler.runRemoteTransforms() } returns false
    }

    @Test
    fun `performCrawl() uses expected chained method calls`() {
        mockkStatic(Stream::class)
        val uri = "mock"
        val mockIntStream = mockk<IntStream>()
        val mockUriStream = mockk<Stream<String>>()
        val mockOptional = mockk<OptionalInt>()

        every { mockOptional.orElse(0) } returns expected
        every { mockUriStream.mapToInt(any()) } returns mockIntStream
        every { mockIntStream.findFirst() } returns mockOptional
        every { Stream.of(uri) } returns mockUriStream
        every { mockUriStream.filter(any()) } returns mockUriStream

        assertThat(mockCrawler.performCrawl(uri, 2)).isEqualTo(expected)

        verify(exactly = 1) {
            mockOptional.orElse(0)
            mockUriStream.mapToInt(any())
            mockUriStream.filter(any())
            Stream.of(uri)
            mockIntStream.findFirst()
        }
    }

    @Test
    fun `performCrawl() stops when maximum depth has been reached`() {
        val uri = "mock"

        mockCrawler.mMaxDepth = 1
        mockCrawler.mUniqueUris = mockUniqueUris

        every {
            mockCrawler.crawlPage(any(), any())
        } throws (RuntimeException("crawlPage should not be called!"))

        every {
            mockCrawler.mUniqueUris.putIfAbsent(any())
        } throws (RuntimeException("mUniqueUris.putIfAbsent() should not be called!"))

        assertThat(mockCrawler.performCrawl(uri, 2)).isEqualTo(0)

        verify(exactly = 0) { mockCrawler.crawlPage(any(), any()) }
        confirmVerified(mockUniqueUris)
    }

    @Test
    fun `performCrawl() runs for all valid depths`() {
        val uri = "mock"
        mockCrawler.mMaxDepth = 3
        mockCrawler.mUniqueUris = mockUniqueUris

        every { mockCrawler.crawlPage(any(), any()) } answers { expected }
        every { mockCrawler.mUniqueUris.putIfAbsent(any()) } answers { true }

        repeat(mockCrawler.mMaxDepth) {
            mockCrawler.performCrawl(uri, it + 1)
        }

        verify(exactly = mockCrawler.mMaxDepth) {
            mockCrawler.performCrawl(any(), any())
            mockUniqueUris.putIfAbsent(any())
            mockCrawler.crawlPage(any(), any())
        }

        confirmVerified(mockUniqueUris)
    }

    @Test
    fun `performCrawl() ignores previously cached uris`() {
        val uri = "mock"

        mockCrawler.mMaxDepth = 3
        mockCrawler.mUniqueUris = mockk<ConcurrentHashSet<String>>()

        every {
            mockCrawler.crawlPage(any(), any())
        } throws (RuntimeException("crawlPage should not be called!"))

        every { mockCrawler.mUniqueUris.putIfAbsent(uri) } returns false

        assertThat(mockCrawler.performCrawl(uri, 3)).isEqualTo(0)

        verify(exactly = 1) { mockCrawler.mUniqueUris.putIfAbsent(uri) }
        confirmVerified(mockCrawler.mUniqueUris)
    }

    @Test
    fun `crawlPage() uses expected chained method calls`() {
        mockkStatic(Stream::class)
        val uri = "mock-url"

        val mockStringStream = mockk<Stream<String>>()
        val mockIntStream = mockk<IntStream>()
        val mockPageStream = mockk<Stream<Crawler.Page>>()
        val mockOptional = mockk<OptionalInt>()
        val mockWebPageCrawler = mockk<WebPageCrawler>()
        mockCrawler.mWebPageCrawler = mockWebPageCrawler

        every { mockStringStream.map<Crawler.Page>(any()) } returns mockPageStream
        every { mockIntStream.findFirst() } returns mockOptional
        every { mockPageStream.filter(any()) } returns mockPageStream
        every { mockOptional.orElse(any()) } returns expected;
        every { Stream.of(uri) } returns mockStringStream
        every { mockPageStream.mapToInt(any()) } returns mockIntStream

        assertThat(mockCrawler.crawlPage(uri, Int.MAX_VALUE)).isEqualTo(expected)

        verify(exactly = 1) {
            Stream.of(uri)
            mockIntStream.findFirst()
            mockStringStream.map<Crawler.Page>(any())
            mockPageStream.mapToInt(any())
            mockOptional.orElse(any())
            mockPageStream.filter(any())
        }
    }

    @Test
    fun `crawlPage() uses expected lambdas`() {
        val uri = "http://www.mock.url/mock-page"
        val mockPage = mockk<Crawler.Page>()
        val mockWebPageCrawler = mockk<WebPageCrawler>()
        mockCrawler.mWebPageCrawler = mockWebPageCrawler

        every { mockWebPageCrawler.getPage(any()) } returns mockPage
        every { mockCrawler.processPage(any(), any()) } returns expected

        assertThat(mockCrawler.crawlPage(uri, Int.MAX_VALUE)).isEqualTo(expected)

        verify(exactly = 1) {
            mockWebPageCrawler.getPage(any())
            mockCrawler.processPage(any(), any())
        }
    }

    @Test
    fun `crawlPage() handles null pages`() {
        val uri = "mock-url"
        val mockWebPageCrawler = mockk<WebPageCrawler>()
        mockCrawler.mWebPageCrawler = mockWebPageCrawler

        every { mockWebPageCrawler.getPage(any()) } returns null
        every { mockCrawler.processPage(any(), any()) } returns 100

        assertThat(mockCrawler.crawlPage(uri, Int.MAX_VALUE)).isEqualTo(0)

        verify(exactly = 0) { mockCrawler.processPage(any(), any()) }
        verify(exactly = 1) { mockWebPageCrawler.getPage(any()) }
    }

    @Test
    fun `processPage() uses expected chained method calls`() {
        val mockPage = mockk<Crawler.Page>()
        val mockIntStream = mockk<IntStream>()

        every { mockIntStream.sum() } returns 98
        every { mockElements.stream() } returns mockPageElementStream
        every { mockPage.getPageElements(any(), any()) } answers {
            val types = it.invocation.args[0] as Array<*>
            assertThat(types[0]).isNotEqualTo(types[1])
            mockElements
        }
        every { mockPageElementStream.mapToInt(any()) } returns mockIntStream

        assertThat(mockCrawler.processPage(mockPage, 99)).isEqualTo(98)

        every { mockPage.getPageElements(any(), any()) } answers {
            val types = it.invocation.args[0] as Array<*>
            assertThat(types[0]).isNotEqualTo(types[1])
            mockElements
        }

        verify(exactly = 1) {
            mockPageElementStream.mapToInt(any())
            mockIntStream.sum()
            mockElements.stream()
        }
    }

    @Test
    fun `processPage() uses expected lambdas`() {
        val mockImageElement = mockk<WebPageElement>()
        val mockPageElement = mockk<WebPageElement>()
        val mockWebPageCrawler = mockk<WebPageCrawler>()
        val mockPage = mockk<Crawler.Page>()

        val mockURL = mockk<URL>()
        val mockUri = "http://www.mock.com/uri"

        every { mockImageElement.type } returns IMAGE
        every { mockImageElement.getURL() } returns mockURL
        every { mockPageElement.type } returns PAGE
        every { mockPageElement.getUrl() } returns mockUri

        val elementStream = spyk(Stream.of(mockImageElement, mockPageElement))
        val pageRoot = "http://www/PAGE/"
        val pageElements = (1..10).map {
            WebPageElement("$pageRoot$it", PAGE)
        }.shuffled()

        val imageRoot = "http://www/IMAGE/"
        val imageElements = (1..10).map {
            WebPageElement("$imageRoot$it", IMAGE)
        }.shuffled()

        val elements = spyk((pageElements + imageElements).shuffled())

        val random = Random()
        val depth = random.nextInt(134)
        val images = random.nextInt(492)
        val pages = random.nextInt(235)

        every { mockPage.getPageElements(any(), any()) } returns elements
        every { elements.stream() } returns elementStream
        every { mockCrawler.performCrawl(mockUri, depth + 1) } returns pages
        every { mockCrawler.processImage(mockURL) } returns images

        mockCrawler.mWebPageCrawler = mockWebPageCrawler
        mockCrawler.mMaxDepth = Int.MAX_VALUE
        mockCrawler.mUniqueUris = mockUniqueUris

        // SUT
        val count = mockCrawler.processPage(mockPage, depth)

        assertThat(count).isEqualTo(images + pages)

        verify(exactly = 1) {
            mockPage.getPageElements(any(), any())
            mockCrawler.processPage(mockPage, depth)
            mockCrawler.performCrawl(mockUri, depth + 1)
            mockCrawler.processImage(mockURL)
            elements.stream()
        }
    }

    @Test
    fun `processImage() uses expected chained method calls`() {
        mockkStatic(Stream::class)
        val mockURLStream = mockk<Stream<URL>>()
        val mockImageStream = mockk<Stream<Image>>()
        val mockIntStream = mockk<IntStream>()

        val url = mockk<URL>()

        every { mockImageStream.filter(any()) } returns mockImageStream
        every { mockImageStream.mapToInt(any()) } returns mockIntStream
        every { mockIntStream.sum() } returns expected
        every { mockURLStream.map<Image>(any()) } returns mockImageStream
        every { Stream.of(url) } returns mockURLStream

        assertThat(mockCrawler.processImage(url)).isEqualTo(expected)

        verify(exactly = 1) {
            mockURLStream.map<Image>(any())
            mockIntStream.sum()
            Stream.of(url)
            mockImageStream.mapToInt(any())
            mockImageStream.filter(any())
        }
    }

    @Test
    fun `processImage() uses expected lambdas`() {
        val imageUrl = "http://www.mock.com/image"
        val url = URL(imageUrl)

        every { mockCrawler.getOrDownloadImage(url) } returns mockImage
        every { mockCrawler.transformImage(mockImage) } returns expected

        assertThat(mockCrawler.processImage(url)).isEqualTo(expected)

        verify(exactly = 1) {
            mockCrawler.getOrDownloadImage(url)
            mockCrawler.transformImage(mockImage)
        }
    }

    @Test
    fun `processImage() handles downloaded image failure`() {
        val imageUrl = "http://www.mock.com/image"
        val url = URL(imageUrl)

        every { mockCrawler.getOrDownloadImage(url) } returns null
        every { mockCrawler.transformImage(mockImage) } throws
                Exception("transformImage() should not be called")
        assertThat(mockCrawler.processImage(url)).isEqualTo(0)
        verify { mockCrawler.getOrDownloadImage(url) }
    }

    @Test
    fun `processImage() handles no transformed images`() {
        val imageUrl = "http://www.mock.com/image"
        val url = URL(imageUrl)

        every { mockCrawler.getOrDownloadImage(url) } returns mockImage
        every { mockCrawler.transformImage(mockImage) } returns 0

        assertThat(mockCrawler.processImage(url)).isEqualTo(0)

        verify(exactly = 1) {
            mockCrawler.getOrDownloadImage(url)
            mockCrawler.transformImage(mockImage)
        }
    }
}

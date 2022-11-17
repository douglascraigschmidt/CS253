package edu.vanderbilt.imagecrawler.crawlers

import admin.AssignmentTests
import edu.vanderbilt.imagecrawler.crawlers.Crawler.Type.IMAGE
import edu.vanderbilt.imagecrawler.crawlers.Crawler.Type.PAGE
import edu.vanderbilt.imagecrawler.platform.Cache
import edu.vanderbilt.imagecrawler.utils.BlockingTask
import edu.vanderbilt.imagecrawler.utils.ExceptionUtils
import edu.vanderbilt.imagecrawler.utils.Image
import edu.vanderbilt.imagecrawler.web.WebPageCrawler
import edu.vanderbilt.imagecrawler.web.WebPageElement
import io.mockk.*
import io.mockk.impl.annotations.MockK
import io.mockk.impl.annotations.SpyK
import org.assertj.core.api.Assertions.assertThat
import org.junit.Before
import org.junit.Test
import java.net.URL
import java.util.*
import java.util.concurrent.ConcurrentHashMap
import java.util.function.Supplier
import java.util.function.ToIntFunction
import java.util.stream.IntStream
import java.util.stream.Stream

class ParallelStreamsCrawlerTests : AssignmentTests() {
    @SpyK
    var crawler = ParallelStreamsCrawler()

    @MockK
    lateinit var uris: ConcurrentHashMap.KeySetView<String, Boolean>

    @MockK
    lateinit var image: Image

    private val expected = -99

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
        val uri = "mock"
        val mockIntStream = mockk<IntStream>()
        val mockUriStream = mockk<Stream<String>>()
        val mockOptional = mockk<OptionalInt>()

        every { mockOptional.orElse(0) } returns expected
        every { mockUriStream.mapToInt(any()) } returns mockIntStream
        every { mockIntStream.findFirst() } returns mockOptional
        every { Stream.of(uri) } returns mockUriStream
        every { mockUriStream.filter(any()) } returns mockUriStream

        assertThat(crawler.performCrawl(uri, 2)).isEqualTo(expected)

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

        crawler.mMaxDepth = 1
        crawler.mUniqueUris = uris

        every {
            crawler.crawlPage(any(), any())
        } throws (RuntimeException("crawlPage should not be called!"))

        every {
            crawler.mUniqueUris.add(any())
        } throws (RuntimeException("mUniqueUris.putIfAbsent() should not be called!"))

        assertThat(crawler.performCrawl(uri, 2)).isEqualTo(0)

        verify(exactly = 0) { crawler.crawlPage(any(), any()) }
        confirmVerified(uris)
    }

    @Test
    fun `performCrawl() runs for all valid depths`() {
        val uri = "mock"
        crawler.mMaxDepth = 3
        crawler.mUniqueUris = uris

        every { crawler.crawlPage(any(), any()) } answers { expected }
        every { crawler.mUniqueUris.add(any()) } answers { true }

        repeat(crawler.mMaxDepth) {
            crawler.performCrawl(uri, it + 1)
        }

        verify(exactly = crawler.mMaxDepth) {
            crawler.performCrawl(any(), any())
            uris.add(any())
            crawler.crawlPage(any(), any())
        }

        confirmVerified(uris)
    }

    @Test
    fun `performCrawl() ignores previously cached uris`() {
        val uri = "mock"

        crawler.mMaxDepth = 3
        crawler.mUniqueUris = mockk<ConcurrentHashMap.KeySetView<String, Boolean>>()

        every {
            crawler.crawlPage(any(), any())
        } throws (RuntimeException("crawlPage should not be called!"))

        every { crawler.mUniqueUris.add(uri) } returns false

        assertThat(crawler.performCrawl(uri, 3)).isEqualTo(0)

        verify(exactly = 1) { crawler.mUniqueUris.add(uri) }
        confirmVerified(crawler.mUniqueUris)
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
        crawler.mWebPageCrawler = mockWebPageCrawler

        every { mockStringStream.map<Crawler.Page>(any()) } returns mockPageStream
        every { mockIntStream.findFirst() } returns mockOptional
        every { mockPageStream.filter(any()) } returns mockPageStream
        every { mockOptional.orElse(any()) } returns expected;
        every { Stream.of(uri) } returns mockStringStream
        every { mockPageStream.mapToInt(any()) } returns mockIntStream

        assertThat(crawler.crawlPage(uri, Int.MAX_VALUE)).isEqualTo(expected)

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
        val mockWebPageCrawler = mockk<WebPageCrawler>()
        val mockPage = mockk<Crawler.Page>()
        crawler.mWebPageCrawler = mockWebPageCrawler

        every { mockWebPageCrawler.getPage(any()) } returns mockPage
        every { crawler.processPage(any(), any()) } returns expected

        assertThat(crawler.crawlPage(uri, Int.MAX_VALUE)).isEqualTo(expected)

        verify(exactly = 1) {
            mockWebPageCrawler.getPage(any())
            crawler.processPage(any(), any())
        }
    }

    @Test
    fun `crawlPage() handles null pages`() {
        val uri = "mock-url"
        val mockWebPageCrawler = mockk<WebPageCrawler>()
        crawler.mWebPageCrawler = mockWebPageCrawler

        every { mockWebPageCrawler.getPage(any()) } returns null
        every { crawler.processPage(any(), any()) } returns 100

        assertThat(crawler.crawlPage(uri, Int.MAX_VALUE)).isEqualTo(0)

        verify(exactly = 0) { crawler.processPage(any(), any()) }
        verify(exactly = 1) { mockWebPageCrawler.getPage(any()) }
    }

    @Test
    fun `processPage() uses expected chained method calls and lambdas`() {
        val input = mockk<Crawler.Page>()
        val output = 99
        val depth = 999
        val count = 99
        val element = mockk<WebPageElement>()
        val e1 = mockk<WebPageElement>()
        val e2 = mockk<WebPageElement>()
        val iStream = mockk<IntStream>()
        val stream = mockk<Stream<WebPageElement>>()
        val s = "mock"
        mockkStatic(ExceptionUtils::class)
        every { ExceptionUtils.rethrowSupplier<URL>(any()) } returns mockk()

        every { e1.getUrl() } answers { s }
        every { e1.type } answers { IMAGE }
        every { e2.getURL() } answers { mockk() }
        every { e2.type } answers { PAGE }
        every { crawler.performCrawl(any(), any()) } answers { count }
        every { crawler.processImage(any()) } answers { count * 2 }
        every { input.getPageElementsAsStream(any(), any()) } answers {
            firstArg<Array<*>>().let {
                assertThat(it[0]).isNotEqualTo(it[1])
            }
            stream
        }
        every { stream.parallel() } answers { stream }
        every { stream.mapToInt(any()) } answers {
            firstArg<ToIntFunction<WebPageElement>>().applyAsInt(e1)
            firstArg<ToIntFunction<WebPageElement>>().applyAsInt(e2)
            iStream
        }
        every { iStream.sum() } answers { output }

        assertThat(crawler.processPage(input, depth)).isEqualTo(output)

        verify(exactly = 1) {
            crawler.processPage(input, depth)
            crawler.performCrawl(any(), any())
            crawler.processImage(any())
            input.getPageElementsAsStream(any(), any())
            stream.parallel()
            stream.mapToInt(any())
            iStream.sum()
        }

        confirmVerified(element, crawler, input, stream, iStream)
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

        assertThat(crawler.processImage(url)).isEqualTo(expected)

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

        every { crawler.getOrDownloadImage(url, any()) } returns image
        every { crawler.transformImage(image) } returns expected

        assertThat(crawler.processImage(url)).isEqualTo(expected)

        verify(exactly = 1) {
            crawler.getOrDownloadImage(url, any())
            crawler.transformImage(image)
        }
    }

    @Test
    fun `processImage() handles downloaded image failure`() {
        val imageUrl = "http://www.mock.com/image"
        val url = URL(imageUrl)

        every { crawler.getOrDownloadImage(url, any()) } returns null
        every { crawler.transformImage(any()) } returns 10
        assertThat(crawler.processImage(url)).isEqualTo(0)
        verify { crawler.getOrDownloadImage(url, any()) }
        verify(exactly = 0) { crawler.transformImage(image) }
    }

    @Test
    fun `processImage() handles no transformed images`() {
        val imageUrl = "http://www.mock.com/image"
        val url = URL(imageUrl)

        every { crawler.getOrDownloadImage(url, any()) } returns image
        every { crawler.transformImage(image) } returns 0
        assertThat(crawler.processImage(url)).isEqualTo(0)

        verify(exactly = 1) {
            crawler.getOrDownloadImage(url, any())
            crawler.transformImage(image)
        }
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

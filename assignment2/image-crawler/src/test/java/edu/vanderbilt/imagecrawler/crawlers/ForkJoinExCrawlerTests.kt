package edu.vanderbilt.imagecrawler.crawlers

import admin.AssignmentTests
import admin.injectInto
import admin.value
import edu.vanderbilt.imagecrawler.platform.Cache
import edu.vanderbilt.imagecrawler.platform.Cache.Item
import edu.vanderbilt.imagecrawler.transforms.Transform
import edu.vanderbilt.imagecrawler.utils.BlockingTask
import edu.vanderbilt.imagecrawler.utils.ExceptionUtils
import edu.vanderbilt.imagecrawler.utils.Image
import edu.vanderbilt.imagecrawler.web.WebPageCrawler
import edu.vanderbilt.imagecrawler.web.WebPageElement
import io.mockk.*
import io.mockk.impl.annotations.SpyK
import org.assertj.core.api.Assertions.assertThat
import org.junit.Before
import org.junit.Test
import java.net.URL
import java.util.*
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.ForkJoinPool
import java.util.concurrent.ForkJoinTask
import java.util.function.*
import java.util.function.Function
import java.util.stream.Collector
import java.util.stream.IntStream
import java.util.stream.Stream

class ForkJoinExCrawlerTests : AssignmentTests() {
    @SpyK
    var crawler = ForkJoinExCrawler()

    @Before
    fun before() {
        every { crawler.log(any(), *anyVararg()) } answers { }
    }

    //Statically mocking ForkJoinPool on mac hangs next run test.
    //@Test
    fun `performCrawl() has the correct solution`() {
        mockkStatic(ForkJoinPool::class)
        val pool = mockk<ForkJoinPool>()
        val expected = 8
        val url = "http://mock.url.com"
        val depth = -99
        val task = mockk<ForkJoinExCrawler.URLCrawlerTask>()
        every { ForkJoinPool.commonPool() } returns pool
        every { crawler.makeURLCrawlerTask(url, depth) } returns task
        every { pool.invoke<Int>(any()) } returns expected
        assertThat(crawler.performCrawl(url, depth)).isEqualTo(expected)

        verify(exactly = 1) {
            ForkJoinPool.commonPool()
            crawler.makeURLCrawlerTask(url, depth)
            pool.invoke<Int>(any())
        }
    }

    @Test
    fun `makePerformTransformTask() has the correct solution`() {
        mockkConstructor(ForkJoinExCrawler.PerformTransformTask::class)
        val image = mockk<Image>()
        val transform = mockk<Transform>()
        assertThat(isMockKMock(crawler.makePerformTransformTask(image, transform))).isTrue
    }

    @Test
    fun `makeProcessImageTask() has the correct solution`() {
        mockkStatic(ExceptionUtils::class)
        val url = mockk<URL>()
        val f = mockk<Function<String, URL>>()
        every { ExceptionUtils.rethrowFunction<String, URL>(any()) } returns f
        every { f.apply(any()) } returns url
        val result = crawler.makeProcessImageTask("mock")
        assertThat(result).isInstanceOf(ForkJoinExCrawler.ProcessImageTask::class.java)
        assertThat(result.value<URL>(URL::class.java)).isSameAs(url)
        verify(exactly = 1) {
            f.apply(any())
            ExceptionUtils.rethrowFunction<String, URL>(any())
        }
    }

    @Test
    fun `makeURLCrawlerTest() has the correct solution`() {
        mockkConstructor(ForkJoinExCrawler.URLCrawlerTask::class)
        val depth = -99
        val result = crawler.makeURLCrawlerTask("mock", depth)
        isMockKMock(result)
    }

    @Test
    fun `URLCrawlerTask compute has correct solution()`() {
        val url = "http://mock.url.com"
        val depth = -99
        val task = mockk<ForkJoinExCrawler.URLCrawlerTask>()
        crawler.injectInto(task, "this$0")
        depth.injectInto(task, "mDepth")
        url.injectInto(task, "mPageUri")
        val expected = -99
        val opt = mockk<OptionalInt>()
        val intStream = mockk<IntStream>()
        val urlStream = mockk<Stream<String>>()
        val mockHashSet = mockk<ConcurrentHashMap.KeySetView<String, Boolean>>()
        mockHashSet.injectInto(crawler)
        crawler.mMaxDepth = 3

        mockkStatic(Stream::class)
        every { opt.orElse(any()) } returns expected
        every { urlStream.mapToInt(any()) } answers {
            arg<ToIntFunction<String>>(0).applyAsInt(url)
            intStream
        }
        every { mockHashSet.add(any()) } returns true
        every { Stream.of(url) } returns urlStream
        every { intStream.findFirst() } returns opt
        every { task.crawlPage(url, any()) } returns expected
        every { urlStream.filter(any()) } answers {
            crawler.mMaxDepth = 3
            repeat(crawler.mMaxDepth + 2) {
                val depth = 1 + it
                depth.injectInto(task, "mDepth")
                arg<Predicate<String>>(0).test(url)
            }
            urlStream
        }

        every { task.compute() } answers { callOriginal() }
        assertThat(task.compute()).isEqualTo(expected)

        verify(exactly = 1) {
            urlStream.mapToInt(any())
            opt.orElse(any())
            intStream.findFirst()
            Stream.of(url)
            urlStream.filter(any())
            task.crawlPage(url, any())
            task.compute()
        }
        verify(exactly = crawler.mMaxDepth) { mockHashSet.add(any()) }
        confirmVerified(urlStream, opt, intStream, task)
    }

    @Test
    fun `URLCrawlerTask crawlPage has correct solution()`() {
        val url = "http://mock.url.com"
        val depth = -99
        val task = mockk<ForkJoinExCrawler.URLCrawlerTask>()
        crawler.injectInto(task, "this$0")
        depth.injectInto(task, "mDepth")
        url.injectInto(task, "mPageUri")
        val expected = -99
        val opt = mockk<OptionalInt>()
        val intStream = mockk<IntStream>()
        val urlStream = mockk<Stream<String>>()
        val pageStream = mockk<Stream<Crawler.Page>>()
        val page = mockk<Crawler.Page>()
        mockkStatic(ForkJoinExCrawler::class)
        mockkStatic(Stream::class)
        val webCrawler = mockk<WebPageCrawler>()
        crawler.mMaxDepth = 3
        crawler.mWebPageCrawler = webCrawler

        every { Stream.of(url) } answers { urlStream }
        every { webCrawler.getPage(url) } answers { page }
        every { urlStream.map<Crawler.Page>(any()) } answers {
            arg<Function<String, Crawler.Page>>(0).apply(url)
            pageStream
        }
        every { pageStream.filter(any()) } answers {
            with(arg<Predicate<Crawler.Page?>>(0)) {
                assertThat(test(null)).isFalse
                assertThat(test(page)).isTrue
            }
            pageStream
        }
        every { task.processPage(page, depth) } answers { expected }
        every { pageStream.mapToInt(any()) } answers {
            arg<ToIntFunction<Crawler.Page>>(0).applyAsInt(page)
            intStream
        }
        every { intStream.findFirst() } answers { opt }
        every { opt.orElse(any()) } answers { expected }
        every { crawler.callInManagedBlocker(any<Supplier<Crawler.Page>>()) } answers {
            firstArg<Supplier<Crawler.Page>>().get()
        }

        every { task.crawlPage(any(), any()) } answers { callOriginal() }
        assertThat(task.crawlPage(url, depth)).isEqualTo(expected)

        verify(exactly = 1) {
            Stream.of(url)
            webCrawler.getPage(url)
            urlStream.map<Crawler.Page>(any())
            pageStream.filter(any())
            task.processPage(page, depth)
            pageStream.mapToInt(any())
            intStream.findFirst()
            opt.orElse(any())
            crawler.callInManagedBlocker(any<Supplier<Crawler.Page>>())
        }

        confirmVerified(webCrawler, urlStream, pageStream, intStream, opt)
    }

    @Test
    fun `URLCrawlerTask processPage has correct solution()`() {
        clearAllMocks()
        val depth = -99
        val task = mockk<ForkJoinExCrawler.URLCrawlerTask>()
        crawler.injectInto(task, "this$0")
        depth.injectInto(task, "mDepth")
        "".injectInto(task, "mPageUri")
        val expected = -99
        val page = mockk<Crawler.Page>()
        mockkStatic(Stream::class)
        crawler.mMaxDepth = 3

        val imageElement = mockk<WebPageElement>()
        val pageElement = mockk<WebPageElement>()
        val urlTask = mockk<ForkJoinExCrawler.URLCrawlerTask>()
        val imageTask = mockk<ForkJoinExCrawler.ProcessImageTask>()
        every { crawler.makeURLCrawlerTask(any(), any()) } returns urlTask
        every { crawler.makeProcessImageTask(any()) } returns imageTask
        every { urlTask.fork() } returns urlTask
        every { imageTask.fork() } returns imageTask
        every { imageElement.type } returns Crawler.Type.IMAGE
        every { pageElement.type } returns Crawler.Type.PAGE
        val stream = mockk<Stream<WebPageElement>>()
        val taskStream = mockk<Stream<ForkJoinExCrawler.ProcessImageTask>>()
        every { stream.map<ForkJoinExCrawler.ProcessImageTask>(any()) } answers {
            with(arg<Function<WebPageElement, ForkJoinExCrawler.ProcessImageTask>>(0)) {
                assertThat(apply(imageElement)).isSameAs(imageTask)
                assertThat(apply(pageElement)).isSameAs(urlTask)
            }
            taskStream
        }
        every { page.getPageElementsAsStream(*anyVararg()) } answers {
            with(arg<Array<Crawler.Type>>(0)) {
                assertThat(this).hasSize(2)
                assertThat(this[0]).isNotEqualTo(this[1])
            }
            stream
        }
        val forkList = mockk<List<ForkJoinTask<Int>>>()
        every { taskStream.collect(any<Collector<Any, Any, Any>>()) } answers {
            forkList
        }
        every { task.sumResults(forkList) } returns expected

        every { task.processPage(any(), any()) } answers { callOriginal() }
        assertThat(task.processPage(page, depth)).isEqualTo(expected)

        verify(exactly = 1) {
            page.getPageElementsAsStream(*anyVararg())
            crawler.makeProcessImageTask(any())
            stream.map<ForkJoinExCrawler.ProcessImageTask>(any())
            imageElement.type
            imageTask.fork()
            taskStream.collect<Array<ForkJoinTask<Int>>, ForkJoinTask<Int>>(any())
            urlTask.fork()
            taskStream.collect(any<Collector<Any, Any, Any>>())
            pageElement.type
            task.sumResults(forkList)
            crawler.makeURLCrawlerTask(any(), any())
        }
    }

    @Test
    fun `URLCrawlerTask sumResults has correct solution()`() {
        val task = mockk<ForkJoinExCrawler.URLCrawlerTask>()
        crawler.injectInto(task, "this$0")
        0.injectInto(task, "mDepth")
        "".injectInto(task, "mPageUri")
        val expected = -99
        mockkStatic(Stream::class)
        val list = mockk<List<ForkJoinTask<Int>>>()
        val stream = mockk<Stream<ForkJoinTask<Int>>>()
        val intStream = mockk<IntStream>()

        every { list.stream() } answers { stream }
        val forkTask = mockk<ForkJoinTask<Int>>()
        every { forkTask.join() } answers { expected }
        every { stream.mapToInt(any()) } answers {
            with(arg<ToIntFunction<ForkJoinTask<Int>>>(0)) {
                applyAsInt(forkTask)
            }
            intStream
        }
        every { intStream.sum() } answers { expected }
        every { task.sumResults(list) } answers { callOriginal() }
        assertThat(task.sumResults(list)).isEqualTo(expected)

        verify(exactly = 1) {
            list.stream()
            forkTask.join()
            stream.mapToInt(any())
            intStream.sum()
        }
    }

    @Test
    fun `ProcessImageTask compute has correct solution()`() {
        val url = mockk<URL>()
        val image = mockk<Image>()
        val task = mockk<ForkJoinExCrawler.ProcessImageTask>()
        crawler.injectInto(task, "this$0")
        url.injectInto(task, "mImageUri")
        val expected = -99
        val mi = mockk<Image>()
        val opt = mockk<OptionalInt>()
        val intStream = mockk<IntStream>()
        val urlStream = mockk<Stream<URL>>()
        mockkStatic(Stream::class)

        every { Stream.of(url) } returns urlStream
        val stream = mockk<Stream<Image>>()
        every { crawler.getOrDownloadImage(any(), any()) } answers {
            secondArg<Consumer<Item>>().accept(mockk())
            image
        }
        every { crawler.managedBlockerDownloadImage(any()) } answers {
            mi
        }
        every { urlStream.map<Image>(any()) } answers {
            arg<Function<URL, Image>>(0).apply(url)
            stream
        }
        every { stream.filter(any()) } answers {
            with(arg<Predicate<Image?>>(0)) {
                assertThat(test(null)).isFalse
                assertThat(test(image)).isTrue
            }
            stream
        }
        every { task.transformImage(any()) } answers { expected }
        every { stream.mapToInt(any()) } answers {
            arg<ToIntFunction<Image>>(0).applyAsInt(image)
            intStream
        }
        every { intStream.findFirst() } answers { opt }
        every { opt.orElse(any()) } answers { expected }

        every { task.compute() } answers { callOriginal() }
        assertThat(task.compute()).isEqualTo(expected)

        verify(exactly = 1) {
            Stream.of(url)
            urlStream.map<Image>(any())
            stream.filter(any())
            crawler.getOrDownloadImage(any(), any())
            task.transformImage(any())
            crawler.managedBlockerDownloadImage(any())
            stream.mapToInt(any())
            intStream.findFirst()
            opt.orElse(any())
            task.compute()
        }
        confirmVerified(crawler, stream, task, intStream, opt)
    }

    @Test
    fun `ProcessImageTask transformImage has correct solution()`() {
        val url = mockk<URL>()
        val image = mockk<Image>()
        val task = mockk<ForkJoinExCrawler.ProcessImageTask>()
        crawler.injectInto(task, "this$0")
        url.injectInto(task, "mImageUri")
        val expected = -99
        val transforms = spyk<List<Transform>>().also { crawler.mTransforms = it }
        val transform = mockk<Transform>()
        val forkTask = mockk<ForkJoinTask<Image>>()
        mockkStatic(Stream::class)

        val forkStream = mockk<Stream<ForkJoinTask<Image>>>()
        val stream = mockk<Stream<Transform>>()
        every { transforms.stream() } returns stream
        every { crawler.makePerformTransformTask(any(), any()) } returns forkTask
        every { forkTask.fork() } returns forkTask
        every { stream.map<ForkJoinTask<Image>>(any()) } answers {
            arg<Function<Transform, Image>>(0).apply(transform)
            forkStream
        }
        val list = mockk<List<ForkJoinTask<Image>>>()
        every { forkStream.collect(any<Collector<Any, Any, Any>>()) } answers {
            list
        }
        every { task.countTransformations(list) } returns expected

        every { task.transformImage(image) } answers { callOriginal() }
        assertThat(task.transformImage(image)).isEqualTo(expected)

        verify(exactly = 1) {
            transforms.stream()
            crawler.makePerformTransformTask(any(), any())
            forkTask.fork()
            stream.map<Image>(any())
            forkStream.collect(any<Collector<Any, Any, Any>>())
            task.countTransformations(list)
        }
    }

    @Test
    fun `ProcessImageTask countTransformations has correct solution()`() {
        val url = mockk<URL>()
        val image = mockk<Image>()
        val task = mockk<ForkJoinExCrawler.ProcessImageTask>()
        crawler.injectInto(task, "this$0")
        url.injectInto(task, "mImageUri")
        val expected = -99L
        val forkTask = mockk<ForkJoinTask<Image>>()
        val list = mockk<List<ForkJoinTask<Image>>>()
        val forkStream = mockk<Stream<ForkJoinTask<Image>>>()
        mockkStatic(Stream::class)

        every { list.stream() } returns forkStream
        every { forkTask.join() } returnsMany listOf(image, null)
        every { forkStream.filter(any()) } answers {
            assertThat(arg<Predicate<ForkJoinTask<Image>>>(0).test(forkTask)).isTrue
            assertThat(arg<Predicate<ForkJoinTask<Image>>>(0).test(forkTask)).isFalse
            forkStream
        }
        every { forkStream.count() } returns expected
        every { task.countTransformations(any()) } answers { callOriginal() }
        assertThat(task.countTransformations(list)).isEqualTo(expected)

        verify(exactly = 1) {
            list.stream()
            forkStream.filter(any())
            task.countTransformations(any())
            forkStream.count()
        }
        verify(exactly = 2) { forkTask.join() }
        confirmVerified(list, forkStream, task)
    }

    @Test
    fun `PerformTransformTask compute has correct solution()`() {
        val image = mockk<Image>()
        val transform = mockk<Transform>()
        val task = mockk<ForkJoinExCrawler.PerformTransformTask>()
        crawler.injectInto(task, "this$0")
        transform.injectInto(task, "mTransform")
        image.injectInto(task, "mImage")

        val expected = mockk<Image>()
        val imageStream = mockk<Stream<Image>>()
        val opt = mockk<Optional<Image>>()
        val stream = mockk<Stream<Transform>>()
        mockkStatic(Stream::class)

        every { Stream.of(transform) } answers { stream }
        every { crawler.createNewCacheItem(any(), any<Transform>()) } answers {
            true
        }
        every { stream.filter(any()) } answers {
            arg<Predicate<Transform>>(0).test(transform)
            stream
        }
        every { crawler.applyTransform(transform, image) } returns image
        every { stream.map<Image>(any()) } answers {
            arg<Function<Transform, Image>>(0).apply(transform)
            imageStream
        }
        every { imageStream.filter(any()) } answers {
            with(arg<Predicate<Image?>>(0)) {
                assertThat(test(null)).isFalse
                assertThat(test(image)).isTrue
            }
            imageStream
        }
        every { imageStream.findFirst() } returns opt
        every { opt.orElse(any()) } answers { expected }

        every { task.compute() } answers { callOriginal() }
        assertThat(task.compute()).isEqualTo(expected)

        verify(exactly = 1) {
            Stream.of(transform)
            crawler.createNewCacheItem(any(), any<Transform>())
            stream.filter(any())
            stream.map<Image>(any())
            crawler.applyTransform(transform, image)
            imageStream.filter(any())
            imageStream.findFirst()
            opt.orElse(any())
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

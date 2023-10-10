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
import io.mockk.clearAllMocks
import io.mockk.confirmVerified
import io.mockk.every
import io.mockk.impl.annotations.SpyK
import io.mockk.isMockKMock
import io.mockk.mockk
import io.mockk.mockkConstructor
import io.mockk.mockkStatic
import io.mockk.slot
import io.mockk.spyk
import io.mockk.verify
import org.assertj.core.api.Assertions.assertThat
import org.junit.Before
import org.junit.Test
import java.net.URL
import java.util.Optional
import java.util.OptionalInt
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.ForkJoinPool
import java.util.concurrent.ForkJoinTask
import java.util.function.Consumer
import java.util.function.Function
import java.util.function.Predicate
import java.util.function.Supplier
import java.util.function.ToIntFunction
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
        depth.injectInto(task)
        url.injectInto(task)
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
            firstArg<ToIntFunction<String>>().applyAsInt(url)
            intStream
        }
        every { mockHashSet.add(any()) } returns true
        every { Stream.of(url) } returns urlStream
        every { intStream.findFirst() } returns opt
        every { task.crawlPage(url, any()) } returns expected

        val filterPredicate = slot<Predicate<String>>()
        every { urlStream.filter(capture(filterPredicate)) } answers {
            urlStream
        }

        every { task.compute() } answers { callOriginal() }
        assertThat(task.compute()).isEqualTo(expected)

        val testCases = listOf(
            Triple(0, 5, true),   // basic case
            Triple(5, 5, true),   // edge case: depth equals max depth
            Triple(6, 5, true),   // edge case: depth greater than max depth
            Triple(0, 5, false),  // edge case: URL already visited
            Triple(5, 5, false),  // edge case: depth equals max depth and URL already visited
            Triple(6, 5, false),  // edge case: depth greater than max depth and URL already visited
            Triple(-1, 5, true),  // edge case: negative depth
            Triple(0, 0, true),   // edge case: zero depth and max depth
            Triple(Int.MAX_VALUE, 5, true), // edge case: large depth value
            Triple(5, Int.MAX_VALUE, true)  // edge case: large max depth value
        )

        var mockHashSetAddCalls = testCases.count { it.first <= it.second }
        for ((mDepth, mMaxDepth, addResult) in testCases) {
            mDepth.injectInto(task)
            crawler.mMaxDepth = mMaxDepth

            every { mockHashSet.add(any()) } returns addResult

            val predicateResult = filterPredicate.captured.test(url)
            val expectedResult = (mDepth <= mMaxDepth) && addResult // The correct lambda logic

            assertThat(predicateResult).isEqualTo(expectedResult)
        }
        verify(exactly = mockHashSetAddCalls) { mockHashSet.add(any()) }

        verify(exactly = 1) {
            urlStream.mapToInt(any())
            opt.orElse(any())
            intStream.findFirst()
            Stream.of(url)
            urlStream.filter(any())
            task.crawlPage(url, any())
            task.compute()
        }
        confirmVerified(urlStream, opt, intStream, task)
    }

    @Test
    fun `URLCrawlerTask crawlPage has correct solution()`() {
        val url = "http://mock.url.com"
        val depth = -99
        val task = mockk<ForkJoinExCrawler.URLCrawlerTask>()
        crawler.injectInto(task, "this$0")
        depth.injectInto(task)
        url.injectInto(task)
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
            firstArg<Function<String, Crawler.Page>>().apply(url)
            pageStream
        }
        every { pageStream.filter(any()) } answers {
            with(firstArg<Predicate<Crawler.Page?>>()) {
                assertThat(test(null)).isFalse
                assertThat(test(page)).isTrue
            }
            pageStream
        }
        every { task.processPage(page, depth) } answers { expected }
        every { pageStream.mapToInt(any()) } answers {
            firstArg<ToIntFunction<Crawler.Page>>().applyAsInt(page)
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
        depth.injectInto(task)
        "".injectInto(task)
        val expected = -99
        val page = mockk<Crawler.Page>()
        mockkStatic(Stream::class)
        crawler.mMaxDepth = 3

        val imageElement = mockk<WebPageElement>()
        val pageElement = mockk<WebPageElement>()
        val urlTask = mockk<ForkJoinExCrawler.URLCrawlerTask>()
        val imageTask = mockk<ForkJoinExCrawler.ProcessImageTask>()
        val forkList = mockk<List<ForkJoinTask<Int>>>()
        val stream = mockk<Stream<WebPageElement>>()
        val taskStream = mockk<Stream<ForkJoinTask<Int>>>()
        every { crawler.makeURLCrawlerTask(any(), any()) } returns urlTask
        every { crawler.makeProcessImageTask(any()) } returns imageTask
        every { urlTask.fork() } returns urlTask
        every { imageTask.fork() } returns imageTask
        every { imageElement.type } returns Crawler.Type.IMAGE
        every { pageElement.type } returns Crawler.Type.PAGE
        every { stream.map(any<Function<WebPageElement, ForkJoinTask<Int>>>()) } answers {
            with(firstArg<Function<WebPageElement, ForkJoinTask<Int>>>()) {
                assertThat(apply(imageElement)).isSameAs(imageTask)
                assertThat(apply(pageElement)).isSameAs(urlTask)
            }
            taskStream
        }
        every { taskStream.toList() } answers {
            forkList
        }

        every { page.getPageElementsAsStream(*anyVararg()) } answers {
            with(firstArg<Array<Crawler.Type>>()) {
                assertThat(this).hasSize(2)
                assertThat(this[0]).isNotEqualTo(this[1])
            }
            stream
        }
        every { taskStream.collect(any<Collector<Any, Any, Any>>()) } answers {
            forkList
        }
        every { task.sumResults(forkList) } returns expected

        every { task.processPage(any(), any()) } answers { callOriginal() }
        assertThat(task.processPage(page, depth)).isEqualTo(expected)

        verify(exactly = 1) {
            page.getPageElementsAsStream(*anyVararg())
            crawler.makeProcessImageTask(any())
            stream.map(any<Function<WebPageElement, ForkJoinTask<Int>>>())
            imageElement.type
            imageTask.fork()
            urlTask.fork()
            pageElement.type
            task.sumResults(forkList)
            crawler.makeURLCrawlerTask(any(), any())
        }

        verifyOneOf(
            "",
            { verify(exactly = 1) { taskStream.toList() } },
            { verify(exactly = 1) { taskStream.collect(any<Collector<Any, Any, Any>>()) } })

    }

    @Test
    fun `URLCrawlerTask sumResults has correct solution()`() {
        val task = mockk<ForkJoinExCrawler.URLCrawlerTask>()
        crawler.injectInto(task, "this$0")
        0.injectInto(task)
        "".injectInto(task)
        val expected = -99
        mockkStatic(Stream::class)
        val list = mockk<List<ForkJoinTask<Int>>>()
        val stream = mockk<Stream<ForkJoinTask<Int>>>()
        val intStream = mockk<IntStream>()

        every { list.stream() } answers { stream }
        val forkTask = mockk<ForkJoinTask<Int>>()
        every { forkTask.join() } answers { expected }
        every { stream.mapToInt(any()) } answers {
            with(firstArg<ToIntFunction<ForkJoinTask<Int>>>()) {
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
        url.injectInto(task)
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
            firstArg<Function<URL, Image>>().apply(url)
            stream
        }
        every { stream.filter(any()) } answers {
            with(firstArg<Predicate<Image?>>()) {
                assertThat(test(null)).isFalse
                assertThat(test(image)).isTrue
            }
            stream
        }
        every { task.transformImage(any()) } answers { expected }
        every { stream.mapToInt(any()) } answers {
            firstArg<ToIntFunction<Image>>().applyAsInt(image)
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
        url.injectInto(task)
        val expected = -99
        val transforms = spyk<List<Transform>>().also { crawler.mTransforms = it }
        val transform = mockk<Transform>()
        val forkTask = mockk<ForkJoinTask<Image>>()
        val forkStream = mockk<Stream<ForkJoinTask<Image>>>()
        val stream = mockk<Stream<Transform>>()
        val list = mockk<List<ForkJoinTask<Image>>>()
        mockkStatic(Stream::class)

        every { transforms.stream() } returns stream
        every { crawler.makePerformTransformTask(any(), any()) } returns forkTask
        every { forkTask.fork() } returns forkTask
        every { stream.map<ForkJoinTask<Image>>(any()) } answers {
            firstArg<Function<Transform, Image>>().apply(transform)
            forkStream
        }

        every { forkStream.toList() } answers { list }
        every { forkStream.collect(any<Collector<Any, Any, Any>>()) } answers { list }
        every { task.countTransformations(list) } returns expected

        every { task.transformImage(image) } answers { callOriginal() }
        assertThat(task.transformImage(image)).isEqualTo(expected)

        verify(exactly = 1) {
            transforms.stream()
            crawler.makePerformTransformTask(any(), any())
            forkTask.fork()
            stream.map<Image>(any())
            task.countTransformations(list)
        }

        verifyOneOf(
            "",
            { verify(exactly = 1) { forkStream.toList() } },
            { verify(exactly = 1) { forkStream.collect(any<Collector<Any, Any, Any>>()) } })
    }

    @Test
    fun `ProcessImageTask countTransformations has correct solution()`() {
        val url = mockk<URL>()
        val image = mockk<Image>()
        val task = mockk<ForkJoinExCrawler.ProcessImageTask>()
        crawler.injectInto(task, "this$0")
        url.injectInto(task)
        val expected = -99L
        val forkTask = mockk<ForkJoinTask<Image>>()
        val list = mockk<List<ForkJoinTask<Image>>>()
        val forkStream = mockk<Stream<ForkJoinTask<Image>>>()
        mockkStatic(Stream::class)

        every { list.stream() } returns forkStream
        every { forkTask.join() } returnsMany listOf(image, null)
        every { forkStream.filter(any()) } answers {
            assertThat(firstArg<Predicate<ForkJoinTask<Image>>>().test(forkTask)).isTrue
            assertThat(firstArg<Predicate<ForkJoinTask<Image>>>().test(forkTask)).isFalse
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
        transform.injectInto(task)
        image.injectInto(task)

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
            firstArg<Predicate<Transform>>().test(transform)
            stream
        }
        every { crawler.applyTransform(transform, image) } returns image
        every { stream.map<Image>(any()) } answers {
            firstArg<Function<Transform, Image>>().apply(transform)
            imageStream
        }
        every { imageStream.filter(any()) } answers {
            with(firstArg<Predicate<Image?>>()) {
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

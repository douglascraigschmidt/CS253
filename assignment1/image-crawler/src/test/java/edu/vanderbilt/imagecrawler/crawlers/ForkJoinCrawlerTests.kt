package edu.vanderbilt.imagecrawler.crawlers

import admin.AssignmentTests
import admin.injectInto
import admin.value
import edu.vanderbilt.imagecrawler.crawlers.ForkJoinCrawler.ProcessImageTask
import edu.vanderbilt.imagecrawler.platform.Cache
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
import io.mockk.verify
import org.assertj.core.api.Assertions.assertThat
import org.junit.Before
import org.junit.Test
import java.net.URL
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.ForkJoinPool
import java.util.concurrent.ForkJoinTask
import java.util.function.Consumer
import java.util.function.Function
import java.util.function.Supplier
import java.util.stream.Stream

class ForkJoinCrawlerTests : AssignmentTests() {
    @SpyK
    var crawler = ForkJoinCrawler()

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
        val task = mockk<ForkJoinCrawler.URLCrawlerTask>()
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
        mockkConstructor(ForkJoinCrawler.PerformTransformTask::class)
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
        assertThat(result).isInstanceOf(ProcessImageTask::class.java)
        assertThat(result.value<URL>(URL::class.java)).isSameAs(url)
        verify(exactly = 1) {
            f.apply(any())
            ExceptionUtils.rethrowFunction<String, URL>(any())
        }
    }

    @Test
    fun `makeURLCrawlerTest() has the correct solution`() {
        mockkConstructor(ForkJoinCrawler.URLCrawlerTask::class)
        val depth = -99
        val result = crawler.makeURLCrawlerTask("mock", depth)
        isMockKMock(result)
    }

    @Test
    fun `URLCrawlerTask compute has correct solution()`() {
        val url = "http://mock.url.com"
        val task = mockk<ForkJoinCrawler.URLCrawlerTask>()
        crawler.injectInto(task, "this$0")
        url.injectInto(task)
        val uris = mockk<ConcurrentHashMap.KeySetView<String, Boolean>>()
        uris.injectInto(crawler)

        val results = listOf(
            mutableMapOf<String, Any>("i" to -1, "m" to 0, "a" to true, "o" to -10),
            mutableMapOf<String, Any>("i" to 0, "m" to 0, "a" to true, "o" to -11),
            mutableMapOf<String, Any>("i" to 0, "m" to -1, "a" to true, "o" to 0),

            mutableMapOf<String, Any>("i" to -1, "m" to 0, "a" to false, "o" to 0),
            mutableMapOf<String, Any>("i" to 0, "m" to 0, "a" to false, "o" to 0),
            mutableMapOf<String, Any>("i" to 0, "m" to -1, "a" to false, "o" to 0),
        )
        results.forEach {
            every { crawler.mUniqueUris.add(any()) } returns it["a"] as Boolean
            every { task.crawlPage(url, any()) } returns it["o"] as Int
            every { task.compute() } answers { callOriginal() }
            it["i"]?.injectInto(task)
            it["m"]?.injectInto(crawler)
            assertThat(task.compute()).isEqualTo(it["o"] as Int)
        }

        verify {
            task.compute()
            crawler.mUniqueUris.add(any())
            task.crawlPage(url, any())
        }
        confirmVerified(crawler, task)
    }

    @Test
    fun `URLCrawlerTask crawlPage has correct solution()`() {
        val url = "http://mock.url.com"
        val depth = -99
        val expected = -99
        val page = mockk<Crawler.Page>()
        val webCrawler = mockk<WebPageCrawler>()
        val task = mockk<ForkJoinCrawler.URLCrawlerTask>()
        crawler.injectInto(task, "this$0")
        depth.injectInto(task)
        url.injectInto(task)
        webCrawler.injectInto(crawler)
        mockkStatic(ForkJoinCrawler::class)
        crawler.mMaxDepth = 3
        crawler.mWebPageCrawler = webCrawler

        every { crawler.callInManagedBlocker(any<Supplier<Crawler.Page?>>()) } answers {
            firstArg<Supplier<Crawler.Page?>>().get()
        }
        every { task.crawlPage(any(), any()) } answers { callOriginal() }
        listOf(
            mutableMapOf("i" to -1, "p" to page, "o" to -10),
            mutableMapOf("i" to -2, "p" to null, "o" to 0)
        ).forEach {
            every { webCrawler.getPage(url) } answers { _ -> it["p"] as Crawler.Page? }
            every { task.processPage(any(), it["i"] as Int) } answers { _ -> it["o"] as Int }
            assertThat(task.crawlPage(url, it["i"] as Int)).isEqualTo(it["o"] as Int)
        }

        verify(exactly = 2) {
            task.crawlPage(any(), any())
            crawler.callInManagedBlocker(any<Supplier<Crawler.Page?>>())
            webCrawler.getPage(url)
        }
        verify(exactly = 1) {
            task.processPage(any(), any())
        }

        confirmVerified(crawler, webCrawler, task)
    }

    @Test
    fun `URLCrawlerTask processPage has correct solution()`() {
        clearAllMocks()
        val depth = -99
        val task = mockk<ForkJoinCrawler.URLCrawlerTask>()
        crawler.injectInto(task, "this$0")
        depth.injectInto(task)
        "".injectInto(task)
        val expected = -99
        val page = mockk<Crawler.Page>()
        mockkStatic(Stream::class)
        crawler.mMaxDepth = 3

        val imageElement = mockk<WebPageElement>()
        val pageElement = mockk<WebPageElement>()
        val urlTask = mockk<ForkJoinCrawler.URLCrawlerTask>()
        val imageTask = mockk<ForkJoinCrawler.ProcessImageTask>()
        every { crawler.makeURLCrawlerTask(any(), any()) } answers { urlTask }
        every { crawler.makeProcessImageTask(any()) } answers { imageTask }
        every { urlTask.fork() } answers { urlTask }
        every { imageTask.fork() } answers { imageTask }
        every { imageElement.type } answers { Crawler.Type.IMAGE }
        every { pageElement.type } answers { Crawler.Type.PAGE }

        every { page.getPageElements(*anyVararg()) } answers {
            with(arg<Array<Crawler.Type>>(0)) {
                assertThat(this).hasSize(2)
                assertThat(this[0]).isNotEqualTo(this[1])
            }
            listOf(imageElement, pageElement)
        }
        every { task.sumResults(any()) } answers {
            expected
        }

        every { task.processPage(any(), any()) } answers { callOriginal() }
        assertThat(task.processPage(page, depth)).isEqualTo(expected)

        verify(exactly = 1) {
            page.getPageElements(*anyVararg())
            crawler.makeProcessImageTask(any())
            imageElement.type
            imageTask.fork()
            urlTask.fork()
            pageElement.type
            task.sumResults(any())
            crawler.makeURLCrawlerTask(any(), any())
        }
    }

    @Test
    fun `URLCrawlerTask sumResults has correct solution()`() {
        val task = mockk<ForkJoinCrawler.URLCrawlerTask>()
        crawler.injectInto(task, "this$0")
        0.injectInto(task)
        "".injectInto(task)
        val expected = -2
        val forkTask = mockk<ForkJoinTask<Int>>()
        val list = listOf(forkTask, forkTask)
        every { forkTask.join() } answers { expected }
        every { task.sumResults(list) } answers { callOriginal() }
        assertThat(task.sumResults(list)).isEqualTo(expected * list.size)

        verify(exactly = 2) {
            forkTask.join()
        }
        verify(exactly = 1) {
            task.sumResults(list)
        }
        confirmVerified(task, forkTask)
    }

    @Test
    fun `ProcessImageTask compute has correct solution()`() {
        val url = mockk<URL>()
        val image = mockk<Image>()
        val task = mockk<ForkJoinCrawler.ProcessImageTask>()
        crawler.injectInto(task, "this$0")
        url.injectInto(task)
        val expected = -99
        val mi = mockk<Image>()

        every { crawler.getOrDownloadImage(any(), any()) } answers {
            secondArg<Consumer<Cache.Item>>().accept(mockk())
            image
        }
        every { crawler.managedBlockerDownloadImage(any()) } answers { mi }
        every { task.transformImage(any()) } answers { expected }

        every { task.compute() } answers { callOriginal() }
        assertThat(task.compute()).isEqualTo(expected)

        verify(exactly = 1) {
            crawler.getOrDownloadImage(any(), any())
            task.transformImage(any())
            crawler.managedBlockerDownloadImage(any())
            task.compute()
        }
        confirmVerified(crawler, task)
    }

    @Test
    fun `ProcessImageTask transformImage has correct solution()`() {
        val url = mockk<URL>()
        val image = mockk<Image>()
        val task = mockk<ForkJoinCrawler.ProcessImageTask>()
        crawler.injectInto(task, "this$0")
        url.injectInto(task)
        val expected = -99
        val transform = mockk<Transform>()
        val transforms = listOf(transform, transform)
        transforms.injectInto(crawler)
        val forkTask = mockk<ForkJoinTask<Image>>()

        every { task.countTransformations(any()) } answers {
            val s = firstArg<List<ForkJoinTask<Image>>>().size
            expected
        }
        every { crawler.makePerformTransformTask(any(), any()) } answers { forkTask }
        every { forkTask.fork() } answers { forkTask }
        every { task.transformImage(image) } answers { callOriginal() }
        assertThat(task.transformImage(image)).isEqualTo(expected)

        verify(exactly = 2) {
            crawler.makePerformTransformTask(any(), any())
            forkTask.fork()
        }
        verify(exactly = 1) {
            task.countTransformations(any())
            task.transformImage(image)
        }
        confirmVerified(crawler, task, forkTask)
    }

    @Test
    fun `ProcessImageTask countTransformations has correct solution()`() {
        val url = mockk<URL>()
        val image = mockk<Image>()
        val task = mockk<ForkJoinCrawler.ProcessImageTask>()
        crawler.injectInto(task, "this$0")
        url.injectInto(task)
        val forkTask = mockk<ForkJoinTask<Image>>()
        val list = listOf(forkTask, forkTask, forkTask, forkTask)
        val images = listOf(image, null, image, null)

        every { forkTask.join() } returnsMany images
        every { task.countTransformations(any()) } answers { callOriginal() }
        assertThat(task.countTransformations(list)).isEqualTo(images.filterNotNull().count())
    }

    @Test
    fun `PerformTransformTask compute has correct solution()`() {
        val image = mockk<Image>()
        val transform = mockk<Transform>()
        val task = mockk<ForkJoinCrawler.PerformTransformTask>()
        val b = listOf(true, false, true)
        val i = listOf(image, null)
        val e = listOf(image, null, null)
        crawler.injectInto(task, "this$0")
        transform.injectInto(task)
        image.injectInto(task)

        every { crawler.createNewCacheItem(any(), any<Transform>()) } returnsMany b
        every { crawler.applyTransform(transform, image) } returnsMany i
        every { task.compute() } answers { callOriginal() }
        repeat(b.size) { assertThat(task.compute()).isEqualTo(e[it]) }
        verify(exactly = b.size) {
            task.compute()
            crawler.createNewCacheItem(any(), any<Transform>())
        }
        verify(exactly = i.size) {
            crawler.applyTransform(transform, image)
        }
        confirmVerified(crawler, task)
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

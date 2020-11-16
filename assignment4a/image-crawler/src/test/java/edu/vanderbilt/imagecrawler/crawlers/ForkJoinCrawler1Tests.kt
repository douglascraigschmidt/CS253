package edu.vanderbilt.imagecrawler.crawlers

import admin.AssignmentTests
import admin.outerClass
import admin.setField
import com.nhaarman.mockitokotlin2.*
import edu.vanderbilt.imagecrawler.transforms.Transform
import edu.vanderbilt.imagecrawler.utils.*
import edu.vanderbilt.imagecrawler.utils.Student.Type.Graduate
import edu.vanderbilt.imagecrawler.utils.Student.Type.Undergraduate
import org.junit.Test
import org.mockito.ArgumentMatchers.anyInt
import org.mockito.ArgumentMatchers.anyString
import org.mockito.InjectMocks
import org.mockito.Mock
import java.net.URL
import java.util.*
import java.util.concurrent.ForkJoinTask
import kotlin.test.assertEquals
import kotlin.test.assertSame
import kotlin.test.fail

class ForkJoinCrawler1Tests : AssignmentTests() {
    @Mock
    lateinit var mockImage: Image

    @Mock
    lateinit var mockTransform: Transform

    @Mock
    lateinit var mockForkJoinImageTask: ForkJoinTask<Image>

    @Mock
    lateinit var mockPageElements: CustomArray<WebPageElement>

    @Mock
    lateinit var mockHashSet: ConcurrentHashSet<String>

    @InjectMocks
    val mockCrawler: ForkJoinCrawler1 = mock()

    //--------------------------------------------------------------

    @Test
    fun testURLCrawlerTaskNoPagesOrImagesUndergraduate() {
        runAs(Undergraduate)
        testURLCrawlerTask(Undergraduate, 0, 0)
    }

    @Test
    fun testURLCrawlerTaskNoPagesOrImagesGraduate() {
        runAs(Graduate)
        testURLCrawlerTask(Graduate, 0, 0)
    }

    //--------------------------------------------------------------

    @Test
    fun testURLCrawlerTaskNoPagesUndergraduate() {
        runAs(Undergraduate)
        testURLCrawlerTask(Undergraduate, 0, 100 + Random().nextInt(100))
    }

    @Test
    fun testURLCrawlerTaskNoPagesGraduate() {
        runAs(Graduate)
        testURLCrawlerTask(Graduate, 0, 100 + Random().nextInt(100))
    }

    //--------------------------------------------------------------

    @Test
    fun testURLCrawlerTaskNoImagesUndergraduate() {
        runAs(Undergraduate)
        testURLCrawlerTask(Undergraduate, 100 + Random().nextInt(100), 0)
    }

    @Test
    fun testURLCrawlerTaskNoImagesGraduate() {
        runAs(Graduate)
        testURLCrawlerTask(Graduate, 100 + Random().nextInt(100), 0)
    }

    //--------------------------------------------------------------

    @Test
    fun testURLCrawlerTaskManyUndergraduate() {
        runAs(Undergraduate)
        val random = Random()
        testURLCrawlerTask(Undergraduate, 100 + random.nextInt(100), 100 + random.nextInt(100))
    }

    @Test
    fun testURLCrawlerTaskGraduateMany() {
        runAs(Graduate)
        val random = Random()
        testURLCrawlerTask(Graduate, 100 + random.nextInt(100), 100 + random.nextInt(100))
    }

    //--------------------------------------------------------------

    @Test
    fun testProcessImageTaskUndergraduate() {
        runAs(Undergraduate)

        testProcessImageTask(Undergraduate)
    }

    @Test
    fun testProcessImageTaskGraduate() {
        runAs(Graduate)

        testProcessImageTask(Graduate)
    }

    //--------------------------------------------------------------

    @Test
    fun testProcessImageTaskWhenDownFails() {
        val url = "http://this-is-a-fake-url-for-testing"
        val task = ForkJoinCrawler1().ProcessImageTask(url)

        task.outerClass = mockCrawler
        doReturn(null).whenever(mockCrawler).getOrDownloadImage(any())

        val result = task.compute()
        assertSame(0, result)

        verify(mockCrawler, times(1)).getOrDownloadImage(any())
    }

    @Test
    fun testPerformTransformTaskNotInCache() {
        val task = ForkJoinCrawler1().PerformTransformTask(mockImage, mockTransform)
        val mockReturnImage = mock<Image>()

        task.outerClass = mockCrawler

        doReturn("foobar").whenever(mockTransform).name
        doReturn(true).whenever(mockCrawler).createNewCacheItem(mockImage, mockTransform)
        doReturn(mockReturnImage).whenever(mockCrawler).applyTransform(mockTransform, mockImage)

        val image = task.compute()
        assertSame(mockReturnImage, image)

        verify(mockCrawler, times(1)).createNewCacheItem(mockImage, mockTransform)
        verify(mockCrawler, times(1)).applyTransform(mockTransform, mockImage)
    }

    @Test
    fun testPerformTransformTaskAlreadyInCache() {
        val task = ForkJoinCrawler1().PerformTransformTask(mockImage, mockTransform)

        task.outerClass = mockCrawler
        doReturn("foobar").whenever(mockTransform).name
        doReturn(false).whenever(mockCrawler).createNewCacheItem(mockImage, mockTransform)

        val image = task.compute()
        assertSame(null, image)

        verify(mockCrawler, times(1)).createNewCacheItem(mockImage, mockTransform)
        verify(mockCrawler, never()).applyTransform(mockTransform, mockImage)
    }

    /**
     * Test helper that handles both Undergraduate and Graduate cases.
     */
    private fun testURLCrawlerTask(type: Student.Type, pages: Int, images: Int) {
        /******* TEST SETUP ************/

        val rootUrl = "/root"
        val imageRet = 1
        val pageRet = 1
        val startDepth = 777
        val maxDepth = Int.MAX_VALUE

        val pageElements = (1..pages).map {
            WebPageElement("http://www/PAGE/$it", Crawler.Type.PAGE)
        }.toMutableList()
        val imageElements = (1..images).map {
            WebPageElement("http://www/IMAGE/$it", Crawler.Type.IMAGE)
        }.toMutableList()
        val elements = (imageElements + pageElements).shuffled().toMutableList()

        val mockPageTasks = mutableListOf<ForkJoinCrawler1.URLCrawlerTask>()
        whenever(mockCrawler.makeURLCrawlerTask(anyString(), anyInt())).thenAnswer {
            val task = mock<ForkJoinCrawler1.URLCrawlerTask>().apply {
                mPageUri = it.arguments[0] as String
                mDepth = it.arguments[1] as Int
                assertEquals(startDepth + 1, mDepth)
                mockPageTasks.add(this)
            }
            doReturn(task).whenever(task).fork()
            doReturn(pageRet).whenever(task).join()
            task
        }

        val mockImageTasks = mutableListOf<ForkJoinCrawler1.ProcessImageTask>()
        whenever(mockCrawler.makeProcessImageTask(anyString())).thenAnswer {
            val task = mock<ForkJoinCrawler1.ProcessImageTask>().also {
                mockImageTasks.add(it)
            }
            doReturn(task).whenever(task).fork()
            doReturn(imageRet).whenever(task).join()
            task
        }

        mockCrawler.mMaxDepth = maxDepth
        mockCrawler.mUniqueUris = mockHashSet
        doReturn(true).whenever(mockHashSet).putIfAbsent(anyString())

        val mockWebPageCrawler = mock<WebPageCrawler>()
        mockCrawler.mWebPageCrawler = mockWebPageCrawler

        val mockPage = mock<Crawler.Page>()
        doReturn(mockPage).whenever(mockWebPageCrawler).getPage(rootUrl)
        doReturn(mockPageElements).whenever(mockPage).getPageElements(Crawler.Type.IMAGE, Crawler.Type.PAGE)

        when (type) {
            // Graduates must use streams.
            Graduate -> doReturn(elements.stream()).whenever(mockPageElements).stream()
            Undergraduate -> {
                // Allow Undergraduates to use streams if they want
                doReturn(elements.stream()).whenever(mockPageElements).stream()
                doCallRealMethod().whenever(mockCrawler).makeForkJoinArray<Image>()
                doReturn(elements.iterator()).whenever(mockPageElements).iterator()
            }
            else -> fail("Assignment.type has not been set")
        }

        val task = mockCrawler.URLCrawlerTask(rootUrl, 1)
        task.mDepth = startDepth

        /******* TEST CALL ************/

        val count = task.compute()

        /******* TEST EVALUATION ************/

        assertEquals(imageRet * images + pageRet * pages, count)

        when (type) {
            Graduate -> {
                verify(mockCrawler, never()).makeForkJoinArray<Image>()
                verify(mockPageElements, times(1)).stream()
            }
            Undergraduate -> if (elements.size > 0) {
                // Undergraduates can use either makeFormJoinArray or streams.
                try {
                    // EITHER for each
                    verify(mockCrawler, times(1)).makeForkJoinArray<Image>()
                } catch (e: Exception) {
                    // OR stream
                    verify(mockPageElements, times(1)).stream()
                }
            }
            else -> fail("Assignment.type has not been set")
        }

        verify(mockWebPageCrawler, times(1)).getPage(rootUrl)
        verify(mockPage, times(1)).getPageElements(Crawler.Type.IMAGE, Crawler.Type.PAGE)

        // Check for the proper number of fork/join calls.
        mockPageTasks.forEach {
            verify(it, times(1)).fork()
            verify(it, times(1)).join()
        }

        mockImageTasks.forEach {
            verify(it, times(1)).fork()
            verify(it, times(1)).join()
        }
    }

    /**
     * Test helper that handles both Undergraduate and Graduate cases.
     */
    private fun testProcessImageTask(type: Student.Type) {
        /******* TEST SETUP ************/

        val url = "http://this-is-a-fake-url-for-testing"

        // Use mock crawler as parent to ProcessImageTask
        // to capture all outer class method calls.
        val imageTask = mockCrawler.ProcessImageTask(url)

        // A real list of transforms
        val transforms = Transform.Factory.newTransforms(Transform.Type.values().toList())

        // The total number of transforms that will be processed.
        val total = transforms.size

        // Set the mTransforms field of the ImageCrawler super class
        // to this real transform list.
        mockCrawler.setField("mTransforms", transforms)

        when (type) {
            Undergraduate -> doCallRealMethod().whenever(mockCrawler).makeForkJoinArray<Image>()
            Graduate -> Unit
            else -> fail("Assignment.type has not been set")
        }

        doReturn(mockImage).whenever(mockCrawler).getOrDownloadImage(any())
        doReturn(mockForkJoinImageTask).whenever(mockCrawler).makePerformTransformTask(any(), any())

        doReturn(mockForkJoinImageTask).whenever(mockForkJoinImageTask).fork()
        doReturn(mockImage).whenever(mockForkJoinImageTask).join()

        /******* TEST CALL ************/

        val result = imageTask.compute()

        /******* TEST EVALUATION ************/

        // Should have processed all the transforms.
        assertSame(total, result)

        // Check for first call.
        verify(mockCrawler, times(1)).getOrDownloadImage(URL(url))

        when (type) {
            // No way to check if Undergraduates decided to use streams.
            Undergraduate -> verify(mockCrawler, times(1)).makeForkJoinArray<Image>()
            Graduate -> verify(mockCrawler, never()).makeForkJoinArray<Image>()
            else -> fail("Assignment.type has not been set")
        }

        // Check for the correct number of calls to makeProcessTransformTask
        // and also ensure that all calls were passed the correct arguments.
        val arg1 = argumentCaptor<Image>()
        val arg2 = argumentCaptor<Transform>()
        verify(mockCrawler, times(total)).makePerformTransformTask(arg1.capture(), arg2.capture())
        // Same image should be used in each new task.
        arg1.allValues.forEach {
            assertEquals(mockImage, it)
        }
        // Each task should use a different transform.
        arg2.allValues.forEachIndexed { i, transform ->
            assertEquals(transforms[i], transform)
        }

        // Check for the proper number of fork/join calls.
        verify(mockForkJoinImageTask, times(total)).fork()
        verify(mockForkJoinImageTask, times(total)).join()
    }
}

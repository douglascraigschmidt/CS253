import edu.vanderbilt.imagecrawler.crawlers.CrawlerType.KOTLIN_COROUTINES
import edu.vanderbilt.imagecrawler.crawlers.CrawlerType.SEQUENTIAL_LOOPS
import junit.framework.TestCase.assertNotNull
import org.junit.Test

class KotlinTest {
    @Test
    fun kotlinCreateNewJavaCrawler() {
        val result = SEQUENTIAL_LOOPS.newInstance()
        assertNotNull(result)
    }

    @Test
    fun kotlinCreateNewKotlinCrawler() {
        val result = KOTLIN_COROUTINES.newInstance()
        assertNotNull(result)
    }
}
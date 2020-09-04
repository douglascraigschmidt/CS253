import org.junit.Test;

import edu.vanderbilt.imagecrawler.crawlers.ImageCrawler;

import static edu.vanderbilt.imagecrawler.crawlers.CrawlerType.KOTLIN_COROUTINES;
import static edu.vanderbilt.imagecrawler.crawlers.CrawlerType.SEQUENTIAL_LOOPS;
import static junit.framework.TestCase.assertNotNull;

public class JavaTest {
    @Test
    public void javaCreateNewJavaCrawler() {
        ImageCrawler result = SEQUENTIAL_LOOPS.newInstance();
        assertNotNull(result);
    }

    @Test
    public void javaCreateNewKotlinCrawler() {
        ImageCrawler result = KOTLIN_COROUTINES.newInstance();
        assertNotNull(result);
    }
}

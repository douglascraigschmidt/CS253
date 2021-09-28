package server.main

import com.fasterxml.jackson.databind.ObjectMapper
import io.mockk.*
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.assertj.core.api.Assertions
import org.assertj.core.api.AssertionsForClassTypes
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.Timeout
import org.mockito.Mockito
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.mock.mockito.MockBean
import org.springframework.cloud.client.discovery.DiscoveryClient
import org.springframework.test.context.ContextConfiguration
import org.springframework.web.client.RestTemplate
import server.common.Components
import server.common.model.TransformedImage
import java.util.*
import java.util.stream.Collector
import java.util.stream.Collectors
import java.util.stream.Stream

/**
 * These use mocking to isolate and test only the service component.
 */
@AutoConfigureMockMvc
@SpringBootTest
@ContextConfiguration(classes = [Components::class, MainApplication::class, MainService::class])
@Timeout(8)
class MainServiceTests {
    @Autowired
    private lateinit var objectMapper: ObjectMapper

    @MockBean
    private lateinit var discoveryClientMock: DiscoveryClient

    private lateinit var mockBackEnd: MockWebServer
    private lateinit var service: MainService

    @BeforeEach
    fun beforeEach() {
        MockKAnnotations.init(this, relaxUnitFun = true)
        mockBackEnd = MockWebServer()
        mockBackEnd.start()
        service = MainService().apply {
            baseUrl = String.format("http://localhost:%d/", mockBackEnd.port)
            restTemplate = RestTemplate()
            discoveryClient = discoveryClientMock
        }
    }

    @AfterEach
    fun tearDown() {
        mockBackEnd.shutdown()
        clearAllMocks()
    }

    @Test
    fun `applyTransforms calls microservices`() {
        val imageBytes = ImageFactory.randomImageBytes()
        val expected = ImageFactory.randomTransformedImages(3)
        val fileName = "foobar.png"

        val transforms = expected.stream()
            .map { obj: TransformedImage -> obj.transformName }
            .collect(Collectors.toUnmodifiableList())

        val services = transforms.stream()
            .map { obj: String -> obj.lowercase(Locale.getDefault()) }
            .collect(Collectors.toUnmodifiableList())

        Mockito.`when`(discoveryClientMock.services).thenReturn(services)
        for (transformedImage in expected) {
            mockBackEnd.enqueue(
                MockResponse()
                    .setBody(objectMapper.writeValueAsString(transformedImage))
                    .addHeader("Content-Type", "application/json")
            )
        }
        val result = service.applyTransforms(
            transforms,
            fileName,
            imageBytes
        )
        result.sortBy { it.transformName }
        expected.sortBy { it.transformName }
        AssertionsForClassTypes.assertThat(result).isEqualTo(expected)
    }

    @Test
    fun `applyTransforms uses expected chained method calls`() {
        val transforms = mockk<List<String>>()
        val fileName = "mock"
        val bytes = ByteArray(2)
        val stringStream4 = mockk<Stream<String>>()
        val stringStream0 = mockk<Stream<String>>()
        val stringStream1 = mockk<Stream<String>>()
        val transformedImageStream = mockk<Stream<TransformedImage>>()
        val expected = listOf(TransformedImage(), TransformedImage())
        val mainService = spyk(MainService())

        every { stringStream1.map<TransformedImage>(any()) } returns transformedImageStream
        every { stringStream0.filter(any()) } returns stringStream1
        every { transforms.parallelStream() } returns stringStream4
        every { stringStream4.map<String>(any()) } returns stringStream0
        every { transformedImageStream.collect(any<Collector<in TransformedImage, Any, Any>>()) } returns expected
        every { transformedImageStream.filter(any()) } returns transformedImageStream

        Assertions.assertThat(mainService.applyTransforms(transforms, fileName, bytes)).isSameAs(expected)

        verify(exactly = 1) {
            stringStream1.map<TransformedImage>(any())
            transformedImageStream.collect(any<Collector<in TransformedImage, Any, Any>>())
            transformedImageStream.filter(any())
            transforms.parallelStream()
            stringStream0.filter(any())
            stringStream4.map<String>(any())
        }
    }

    @Test
    fun `applyTransforms uses expected lambdas`() {
        val transforms = mockk<List<String>>()
        val fileName = "mock"
        val bytes = ByteArray(2)
        val mainService = spyk(MainService())
        val transformedImage = mockk<TransformedImage>()

        val stringStream = spyk(Stream.of("one", "two", "three"))
        every { mainService.postRequest(any(), any(), any()) } returns transformedImage
        every { mainService.buildMicroserviceUrl(any()) } returns "mock"
        every { transforms.parallelStream() } returns stringStream

        Assertions.assertThat(mainService.applyTransforms(transforms, fileName, bytes))
            .containsSequence(transformedImage, transformedImage, transformedImage)

        verify(exactly = 3) {
            mainService.postRequest(any(), any(), any())
            mainService.buildMicroserviceUrl(any())
        }
        verify(exactly = 1) { transforms.parallelStream() }
    }
}
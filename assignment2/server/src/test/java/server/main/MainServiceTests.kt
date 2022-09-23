package server.main

import com.fasterxml.jackson.databind.ObjectMapper
import io.mockk.*
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.assertj.core.api.Assertions
import org.assertj.core.api.Assertions.assertThat
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
            .map { obj: String -> obj.lowercase(Locale.US) }
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
            imageBytes,
            false
        )
        result.sortBy { it.transformName }
        expected.sortBy { it.transformName }
        AssertionsForClassTypes.assertThat(result).isEqualTo(expected)
    }

    @Test
    fun `applyTransforms sequentially uses expected chained method calls`() {
        testApplyTransformsChain(false)
    }

    @Test
    fun `applyTransforms in parallel uses expected chained method calls`() {
        testApplyTransformsChain(true)
    }

    @Test
    fun `applyTransforms sequentially uses expected lambdas`() {
        testApplyTransformsLambdas(false)
    }

    @Test
    fun `applyTransforms in parallel uses expected lambdas`() {
        testApplyTransformsLambdas(true)
    }

    private fun testApplyTransformsChain(parallel: Boolean) {
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
        if (parallel) {
            every { transforms.parallelStream() } answers {
                stringStream4
            }
            every { transforms.stream() } answers {
                throw Exception("stream() should not be called")
            }
        } else {
            every { transforms.parallelStream() } answers {
                throw Exception("parallelStream() should not be called")
            }

            every { transforms.stream() } answers {
                stringStream4
            }
        }
        every { stringStream4.map<String>(any()) } returns stringStream0
        every { transformedImageStream.collect(any<Collector<in TransformedImage, Any, Any>>()) } returns expected
        every { transformedImageStream.filter(any()) } returns transformedImageStream

        assertThat(mainService.applyTransforms(transforms, fileName, bytes, parallel)).isSameAs(expected)

        verify(exactly = 1) {
            stringStream1.map<TransformedImage>(any())
            transformedImageStream.collect(any<Collector<in TransformedImage, Any, Any>>())
            transformedImageStream.filter(any())
            stringStream0.filter(any())
            stringStream4.map<String>(any())
        }
        verify(exactly = if (parallel) 1 else 0) { transforms.parallelStream() }
        verify(exactly = if (parallel) 0 else 1) { transforms.stream() }
    }

    @Test
    fun `postRequest uses the correct REST call`() {
        val mockService = spyk<MainService>()
        val mockUrl = "http://a.b.com"
        val mockFileName = "mock-file-name"
        val mockBytes = byteArrayOf(0, 1, 2)
        val mockTemplate = mockk<RestTemplate>()
        val mockImage = mockk<TransformedImage>()
        mockService.restTemplate = mockTemplate
        every { mockTemplate.postForObject<TransformedImage>(mockUrl, any(), any()) } returns mockImage
        assertThat(mockService.postRequest(mockUrl, mockBytes, mockFileName)).isSameAs(mockImage)
        verify {
            mockTemplate.postForObject<TransformedImage>(mockUrl, any(), any())
        }
    }

    private fun testApplyTransformsLambdas(parallel: Boolean) {
        val transforms = mockk<List<String>>()
        val fileName = "mock"
        val bytes = ByteArray(2)
        val mainService = spyk(MainService())
        val transformedImage = mockk<TransformedImage>()
        val stringStream = spyk(Stream.of("one", "two", "three"))

        every { mainService.postRequest(any(), any(), any()) } returns transformedImage
        every { mainService.buildMicroserviceUrl(any()) } returns "mock"
        if (parallel) {
            every { transforms.parallelStream() } returns stringStream
        } else {
            every { transforms.stream() } returns stringStream
        }

        assertThat(mainService.applyTransforms(transforms, fileName, bytes, parallel))
            .containsSequence(transformedImage, transformedImage, transformedImage)

        verify(exactly = 3) {
            mainService.postRequest(any(), any(), any())
            mainService.buildMicroserviceUrl(any())
        }

        verify(exactly = parallel.let { if (it) 1 else 0 }) { transforms.parallelStream() }
        verify(exactly = parallel.let { if (it) 0 else 1 }) { transforms.stream() }
    }

    @Test
    fun `getTransformMicroServices() uses expected chained method calls`() {
        val mainService = spyk(MainService())
        val mockClient = mockk<DiscoveryClient>().also { mainService.discoveryClient = it }
        val mockList = mockk<List<String>>()
        val mockStream = mockk<Stream<String>>()
        every { mockList.stream() } answers  { mockStream }
        every { mockClient.services } answers  { mockList }
        assertThat(mainService.transformMicroServices).isSameAs(mockStream)
        verify {
            mockList.stream()
            mainService.transformMicroServices
            mockClient.services
        }
    }
}
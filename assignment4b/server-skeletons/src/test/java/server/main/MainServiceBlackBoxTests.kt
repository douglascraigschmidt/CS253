package server.main

import com.fasterxml.jackson.databind.ObjectMapper
import io.mockk.MockKAnnotations
import io.mockk.clearAllMocks
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.assertj.core.api.Assertions
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.Mockito
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.mock.mockito.MockBean
import org.springframework.cloud.client.discovery.DiscoveryClient
import org.springframework.test.context.ContextConfiguration
import org.springframework.web.reactive.function.client.WebClient
import reactor.test.StepVerifier
import server.common.Components
import server.common.model.TransformedImage
import java.util.*
import java.util.stream.Collectors

/**
 * These use mocking to isolate and test only the service component.
 */
@AutoConfigureMockMvc
@SpringBootTest
@ContextConfiguration(classes = [Components::class, MainApplication::class, MainService::class])
class MainServiceBlackBoxTests {
    @Autowired
    private val objectMapper: ObjectMapper? = null

    @MockBean
    private val discoveryClientMock: DiscoveryClient? = null
    private var mockBackEnd: MockWebServer? = null
    private var service: MainService? = null

    @BeforeEach
    fun beforeEach() {
        MockKAnnotations.init(this, relaxUnitFun = true)
        mockBackEnd = MockWebServer()
        mockBackEnd!!.start()
        service = MainService()
        service!!.baseUrl = String.format("http://localhost:%d/", mockBackEnd!!.port)
        service!!.webClient = WebClient.builder().build()
        service!!.discoveryClient = discoveryClientMock
    }

    @AfterEach
    fun tearDown() {
        mockBackEnd!!.shutdown()
        clearAllMocks()
    }

    @Test
    fun applyTransformsBlackBoxTest() {
        val imageBytes = ImageFactory.randomImageBytes()
        val expected = ImageFactory.randomTransformedImages(3)
        val fileName = "foobar.png"
        val transforms = expected.stream()
            .map { obj: TransformedImage? -> obj!!.transformName }
            .collect(Collectors.toUnmodifiableList())
        Mockito.`when`(discoveryClientMock!!.services).thenReturn(
            transforms
                .stream()
                .map { obj: String -> obj.lowercase(Locale.getDefault()) }
                .collect(Collectors.toUnmodifiableList())
        )
        for (transformedImage in expected) {
            mockBackEnd!!.enqueue(
                MockResponse()
                    .setBody(objectMapper!!.writeValueAsString(transformedImage))
                    .addHeader("Content-Type", "application/json")
            )
        }

        // The service uses a parallel scheduler to forward requests to
        // microservices in an arbitrary order and therefore the resulting
        // Flux of Flight objects will also have an arbitrary order. The
        // StepVerifier only provides a consumeNextSequence which does not
        // support unordered comparisons. Therefore, it's necessary to check
        // each Flight individually by using a loop to add the correct
        // number of consumeWithNext() calls.
        val stepVerifier = StepVerifier.create(
            service!!.applyTransforms(
                transforms,
                fileName,
                imageBytes,
                false
            )
        )
        for (i in expected.indices) {
            stepVerifier.consumeNextWith { transformedImage: TransformedImage ->
                Assertions.assertThat(transformedImage).isIn(expected)
                Assertions.assertThat(expected.remove(transformedImage)).isTrue()
            }
        }
        stepVerifier.verifyComplete()


        // Ensure that all expected flights were received.
        Assertions.assertThat(expected).isEmpty()
    }
}
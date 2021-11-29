package server.main

import io.mockk.*
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.Timeout
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.cloud.client.discovery.DiscoveryClient
import org.springframework.test.context.ContextConfiguration
import org.springframework.web.reactive.function.client.WebClient
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import reactor.core.scheduler.Scheduler
import reactor.core.scheduler.Schedulers
import server.common.Components
import server.common.model.TransformedImage

/**
 * These use mocking to isolate and test only the service component.
 */
@AutoConfigureMockMvc
@SpringBootTest
@ContextConfiguration(classes = [Components::class, MainApplication::class, MainService::class])
@Timeout(8)
class MainServiceWhiteBoxTests {
    @BeforeEach
    fun beforeEach() {
        MockKAnnotations.init(this, relaxUnitFun = true)
    }

    @AfterEach
    fun afterEach() {
        clearAllMocks()
    }

    @Test
    fun `applyTransforms parallel uses correct chained method calls`() {
        val service = spyk<MainService>()
        mockkStatic(Flux::class)
        mockkStatic(Schedulers::class)
        val f = mockk<Flux<String>>()
        val result = mockk<Flux<TransformedImage>>()
        val s = mockk<Scheduler>()

        every { Flux.fromIterable<String>(any()) } returns f
        every { f.subscribeOn(any()) } returns f
        every { Schedulers.parallel() } returns s
        every { Schedulers.boundedElastic() } returns s
        every { Schedulers.newParallel(any()) } returns s
        every { f.flatMap<String>(any()) } returns f
        every { f.flatMap<TransformedImage>(any()) } returns result

        service.applyTransforms(
            listOf("mock1", "mock2"),
            "mock1",
            ByteArray(10),
            true
        )

        verify(exactly = 1) {
            Flux.fromIterable<String>(any())
            f.subscribeOn(any())
            f.flatMap<String>(any())
            f.flatMap<TransformedImage>(any())
            Schedulers.boundedElastic()
        }

        confirmVerified(f, Schedulers::class.java, Flux::class.java)
    }

    @Test
    fun `applyTransforms sequential uses correct chained method calls`() {
        val service = spyk<MainService>()
        val result = mockk<Flux<TransformedImage>>()
        mockkStatic(Flux::class)
        val fx = mockk<Flux<String>>()
        val fy = mockk<Flux<String>>()
        mockkStatic(Schedulers::class)

        every { fx.flatMap<TransformedImage>(any()) } returns result
        every { Flux.fromIterable<String>(any()) } returns fy
        every { fy.flatMap<String>(any()) } returns fx

        service.applyTransforms(
            listOf("mock1", "mock2"),
            "mock1",
            ByteArray(10),
            false
        )

        verify(exactly = 1) {
            fx.flatMap<TransformedImage>(any())
            Flux.fromIterable<String>(any())
            fy.flatMap<String>(any())
        }

        confirmVerified(fx, fy, Schedulers::class.java, Flux::class.java)
    }

    @Test
    fun `postRequest uses correct chained method calls`() {
        val service = spyk<MainService>()

        val wc = mockk<WebClient>()
        val sf = mockk<WebClient.RequestBodyUriSpec>()
        val sa = mockk<WebClient.RequestBodySpec>()
        val sc = mockk<WebClient.RequestHeadersSpec<*>>()
        val sb = mockk<WebClient.ResponseSpec>()
        val result = mockk<Mono<TransformedImage>>()
        every { sb.bodyToMono(any<Class<*>>()) } returns result
        every { sf.uri(any<String>()) } returns sa
        every { sa.bodyValue(any()) } returns sc
        every { sc.retrieve() } returns sb
        every { wc.post() } returns sf

        service.webClient = wc
        service.postRequest("mock", byteArrayOf(0), "mock")

        verify(exactly = 1) {
            sa.bodyValue(any())
            sc.retrieve()
            sb.bodyToMono(any<Class<*>>())
            sf.uri(any<String>())
            wc.post()
        }
    }

    @Test
    fun `getTransformedMicroservices returns correct value`() {
        val service = spyk<MainService>()
        val f = mockk<Flux<String>>()
        val c = mockk<DiscoveryClient>()
        val s = mockk<List<String>>()
        mockkStatic(Flux::class)
        every { c.services } returns s
        every { Flux.fromIterable<String>(any()) } returns f

        service.discoveryClient = c
        assertThat(service.transformMicroServices).isSameAs(f)

        verify {
            c.services
            Flux.fromIterable<String>(any())
        }
    }
}
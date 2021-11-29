package server.microservices.common;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.http.MediaType;
import org.springframework.http.client.MultipartBodyBuilder;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.springframework.web.reactive.function.BodyInserters;
import server.common.model.TransformedImage;
import server.microservices.grayscale.GrayScaleMicroservice;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.mockito.Mockito.*;
import static server.common.Constants.EndPoint.APPLY_TRANSFORM;
import static server.main.ImageFactory.randomImageBytes;
import static server.main.ImageFactory.randomTransformedImage;

@EnableDiscoveryClient(autoRegister = false)
@SpringBootTest(classes = {GrayScaleMicroservice.class},
        webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT)
@AutoConfigureMockMvc
public class TransformControllerTests {
    @Autowired
    WebTestClient webTestClient;

    @MockBean
    private TransformService service;

    @Test
    public void testApplyTransform() {
        byte[] imageBytes = randomImageBytes();
        TransformedImage expected = randomTransformedImage();
        String fileName = "foobar.png";

        MultipartBodyBuilder multipartBodyBuilder = new MultipartBodyBuilder();

        multipartBodyBuilder
                .part("image", imageBytes)
                .filename(fileName);

        when(service
                .applyTransform(
                        fileName,
                        expected.getTransformName(),
                        imageBytes))
                .thenReturn(expected);

        TransformedImage result =
                webTestClient
                        .post()
                        .uri("/" + APPLY_TRANSFORM +
                                "?transform=" + expected.getTransformName())
                        .body(BodyInserters.fromMultipartData(
                                multipartBodyBuilder.build()))
                        .accept(MediaType.APPLICATION_JSON)
                        .exchange()
                        .expectStatus().isOk()
                        .expectBody(TransformedImage.class)
                        .returnResult()
                        .getResponseBody();

        verify(service, times(1))
                .applyTransform(
                        fileName,
                        expected.getTransformName(),
                        imageBytes);

        assertThat(result).isEqualTo(expected);

        clearInvocations(service);
    }
}
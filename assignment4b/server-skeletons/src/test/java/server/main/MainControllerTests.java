package server.main;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.http.client.MultipartBodyBuilder;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.springframework.web.reactive.function.BodyInserters;
import reactor.core.publisher.Flux;
import server.common.model.TransformedImage;

import java.util.List;
import java.util.stream.Collectors;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.mockito.Mockito.*;
import static server.common.Constants.EndPoint.APPLY_TRANSFORMS;
import static server.main.ImageFactory.randomImageBytes;
import static server.main.ImageFactory.randomTransformedImages;

@SpringBootTest(classes = MainApplication.class,
        webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT)
@AutoConfigureMockMvc
public class MainControllerTests {

    @Autowired
    WebTestClient webTestClient;

    @MockBean
    private MainService service;

    @Test
    public void testApplyTransforms() {
        byte[] imageBytes = randomImageBytes();
        List<TransformedImage> expected = randomTransformedImages(3);
        String fileName = "foobar.png";
        List<String> transforms =
                expected.stream()
                        .map(TransformedImage::getTransformName)
                        .collect(Collectors.toUnmodifiableList());

        when(service
                .applyTransforms(transforms, fileName, imageBytes, false))
                .thenReturn(Flux.fromIterable(expected));

        MultipartBodyBuilder multipartBodyBuilder = new MultipartBodyBuilder();

        multipartBodyBuilder
                .part("image", imageBytes)
                .filename(fileName);

        List<TransformedImage> result =
                webTestClient
                        .post()
                        .uri("/" + APPLY_TRANSFORMS
                                + "?transforms=" + transforms.get(0)
                                + "&transforms=" + transforms.get(1)
                                + "&transforms=" + transforms.get(2)
                                + "&parallel=false")
                        .body(BodyInserters.fromMultipartData(
                                multipartBodyBuilder.build()))
                        .accept(MediaType.APPLICATION_JSON)
                        .exchange()
                        .expectStatus().isOk()
                        .expectBodyList(TransformedImage.class)
                        .returnResult()
                        .getResponseBody();

        verify(service, times(1))
                .applyTransforms(transforms, fileName, imageBytes, false);

        assertThat(result).isEqualTo(expected);

        clearInvocations(service);
    }
}

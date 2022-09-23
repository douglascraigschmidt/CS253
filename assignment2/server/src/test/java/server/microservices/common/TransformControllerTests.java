package server.microservices.common;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;
import server.common.model.TransformedImage;
import server.microservices.grayscale.GrayScaleMicroservice;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static server.common.Constants.EndPoint.APPLY_TRANSFORM;
import static server.main.ImageFactory.randomImageBytes;
import static server.main.ImageFactory.randomTransformedImage;


@EnableDiscoveryClient(autoRegister = false)
@SpringBootTest(
        classes = {
                GrayScaleMicroservice.class,
                TransformController.class
        },
        webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT)
@AutoConfigureMockMvc
public class TransformControllerTests {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private TransformService service;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    public void testApplyTransforms() throws Exception {

        byte[] imageBytes = randomImageBytes();
        TransformedImage expected = randomTransformedImage();
        String fileName = expected.getImageName();

        when(service
                .applyTransform(expected.getImageName(), expected.getTransformName(), imageBytes))
                .thenReturn(expected);

        MockMultipartFile file =
                new MockMultipartFile(
                        "image",
                        fileName,
                        MediaType.APPLICATION_OCTET_STREAM_VALUE,
                        imageBytes
                );

        String jsonResult =
                mockMvc.perform(multipart("/" + APPLY_TRANSFORM)
                                .file(file)
                                .queryParam("transform", expected.getTransformName()))
                        .andExpect(status().isOk())
                        .andReturn()
                        .getResponse()
                        .getContentAsString();

        TransformedImage result = objectMapper.readValue(
                jsonResult, new TypeReference<TransformedImage>() {
                });

        verify(service, times(1))
                .applyTransform(
                        fileName,
                        expected.getTransformName(),
                        imageBytes);

        assertThat(result).isEqualTo(expected);

        clearInvocations(service);
    }
}
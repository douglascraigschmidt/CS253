package server.main;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.junit.Ignore;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;
import server.common.model.TransformedImage;

import java.util.List;
import java.util.stream.Collectors;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static server.common.Constants.EndPoint.APPLY_TRANSFORMS;
import static server.main.ImageFactory.randomImageBytes;
import static server.main.ImageFactory.randomTransformedImages;


@SpringBootTest(
        classes = MainApplication.class,
        webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT)
@AutoConfigureMockMvc
public class MainControllerTests {
    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private MainService service;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    public void testApplyTransforms() throws Exception {

        byte[] imageBytes = randomImageBytes();
        List<TransformedImage> expected = randomTransformedImages(3);
        String fileName = "foobar.png";
        List<String> transforms =
                expected.stream()
                        .map(TransformedImage::getTransformName)
                        .collect(Collectors.toUnmodifiableList());

        when(service
                .applyTransforms(transforms, fileName, imageBytes, false))
                .thenReturn(expected);

        MockMultipartFile file =
                new MockMultipartFile(
                        "image",
                        fileName,
                        MediaType.APPLICATION_OCTET_STREAM_VALUE,
                        imageBytes
                );

        String jsonResult = mockMvc.perform(multipart("/" + APPLY_TRANSFORMS)
                .file(file)
                .queryParam("transforms", transforms.get(0))
                .queryParam("transforms", transforms.get(1))
                .queryParam("transforms", transforms.get(2))
                .queryParam("parallel", "false")
                .queryParam("imageName", fileName))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        List<TransformedImage> result = objectMapper.readValue(
                jsonResult, new TypeReference<List<TransformedImage>>() {
                });

        verify(service, times(1))
                .applyTransforms(transforms, fileName, imageBytes, false);

        assertThat(result).isEqualTo(expected);

        clearInvocations(service);
    }
}
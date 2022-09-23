package server.main;

import org.checkerframework.checker.nullness.qual.Nullable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.client.discovery.DiscoveryClient;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;
import server.common.model.TransformedImage;

import java.util.List;
import java.util.Objects;
import java.util.stream.Stream;

import static java.util.stream.Collectors.toList;

/**
 * This class defines implementation methods that are called by the
 * {@link MainController}, which serves as the main "front-end"
 * app gateway entry point for remote clients that want an image
 * to be processed by a set of image transforms.
 *
 * A {@link DiscoveryClient} is used to redirect calls to the
 * appropriate microservices, which can run in processes that are
 * deployed to other computers in a cluster.
 *
 * This class is annotated as a Spring {@code @Service}, which enables
 * the auto-detection and wiring of dependent implementation classes
 * via classpath scanning.
 */
@Service
public class MainService {
    /**
     * This auto-wired field connects the {@link MainService} to
     * a {@link RestTemplate} used to redirect all HTTP requests to
     * the appropriate microservices.
     */
    @Autowired
    RestTemplate restTemplate;

    /**
     * This auto-wired field connects the {@link MainService} to
     * the {@link DiscoveryClient} used to find all registered
     * microservices.
     */
    @Autowired
    DiscoveryClient discoveryClient;

    /**
     * Tests can set this value for mocking a back-end server.
     */
    String baseUrl = "http://";

    /**
     * Applies the given {@link List} of {@code transforms} to the
     * given {@code image} and return a {@link List} of {@link
     * TransformedImage} objects.
     *
     * @param transforms A {@link List} of transforms to apply
     * @param fileName   Image file name
     * @param imageBytes Image content bytes
     * @param parallel   True if transforms should be applied in
     *                   parallel, else false
     * @return A {@link List} of {@link TransformedImage} objects
     */
    public List<TransformedImage> applyTransforms(List<String> transforms,
                                                  String fileName,
                                                  byte[] imageBytes,
                                                  Boolean parallel) {
        // Create and use a Java Stream as follows:
        // 1. Convert the List of transforms into either a sequential
        //    or parallel Stream.
        // 2. Build a request URL for this microservice.
        // 3. Skip transforms that have no matching running microservice.
        // 4. Send a POST request to the service via the restTemplate and
        //    extract the body from the returned ResponseEntity.
        // 5. Skip any null (failed) transformed image.
        // 6. Collect all transformed images into a List and
        //    return it.

        // Send a transform request to each microservice and then
        // return the list of all transformed images.
        // TODO -- you fill in here replacing this statement with your solution.
        return null;
    }

    /**
     * Send a POST request to the specified url using a multi-value
     * map that contains the passed image bytes and image file name.
     *
     * @param url        The microservice url
     * @param imageBytes The image bytes to post
     * @param fileName   A file name where returned image will be stored
     * @return A transformed image
     */
    protected TransformedImage postRequest(String url,
                                           byte[] imageBytes,
                                           String fileName) {
        // Create a multi-value map containing the image filename and
        // bytes.
        MultiValueMap<String, Object> map = new LinkedMultiValueMap<>();

        // Byte arrays in multi value maps require overriding
        // getFileName() in a ByteArrayResource and adding that
        // resource into the value map.
        ByteArrayResource byteArrayResource =
            new ByteArrayResource(imageBytes) {
                @Override
                public String getFilename() {
                    return fileName;
                }
            };

        // Add the byte array to the map.
        map.add("image", byteArrayResource);

        // Post the request and return the transformed image.
        // TODO -- you fill in here replacing this statement with your
        // solution.
        return null;
    }

    /**
     * Builds a microservice redirect URL for the given {@code
     * transform} name.
     *
     * @param transform The transform name
     * @return A microservice redirect URL or null if the transform
     * microservice is not running
     */
    @Nullable
    protected String buildMicroserviceUrl(String transform) {
        // Get a stream of all registered microservices.
        return getTransformMicroServices()
            // Only allow the microservice that performs the given
            // image transform.
            .filter(microservice ->
                    microservice.contains(transform.toLowerCase()))

            // Construct the redirect URL for this transform
            // microservice.
            .map(___ -> baseUrl
                 + transform.toLowerCase()
                 + "/apply-transform?transform="
                 + transform)

            // Call a terminal operation to handle first (only) element.
            .findFirst()

            // Return null if there's no running microservice for this
            // transform.
            .orElse(null);
    }

    /**
     * Use the Eureka discovery client to return all microservices.
     *
     * @return A {@link Stream} containing all registered
     * microservices
     */
    protected Stream<String> getTransformMicroServices() {
        // Return a Stream containing all registered microservices
        // that transform images.
        // TODO -- you fill in here replacing this statement with your
        // solution.
        return null;
    }
}

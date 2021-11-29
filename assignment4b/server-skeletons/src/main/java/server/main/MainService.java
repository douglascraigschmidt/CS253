package server.main;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.client.discovery.DiscoveryClient;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;
import server.common.model.TransformedImage;

import java.util.List;
import java.util.Objects;

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
     * a {@link WebClient} used to redirect all HTTP requests to
     * the appropriate microservices.
     */
    @Autowired
    WebClient webClient;

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
     * Apply the given {@link List} of {@code transforms} to the given
     * image and return a {@link Flux} that emits the {@link
     * TransformedImage} objects.
     *
     * @param transforms A list of transforms to apply
     * @param fileName   Image file name
     * @param imageBytes Image content bytes
     * @param parallel   True if transforms should be applied in
     *                   parallel, else false
     * @return A {@link Flux} that emits of {@link TransformedImage} objects
     */
    public Flux<TransformedImage> applyTransforms(List<String> transforms,
                                                  String fileName,
                                                  byte[] imageBytes,
                                                  Boolean parallel) {
        // Create and use a Flux as follows:
        // 1. Create a flux that is either sequential or parallel 
        //    based on the 'parallel' input parameter.
        // 2. Build a request URL for this microservice.
        // 3. Send a POST request to the service via postRequest() and
        //    return the resulting Flux of TransformedImage objects.

        // TODO -- you fill in here replacing 'return null' with your
        // solution.

        return null;
    }

    /**
     * Builds a microservice redirect URL for the given {@code
     * transform} name.
     *
     * @param transform The transform name

     * @return A {@link Mono} that emits the microservice redirect URL
     * or an empty {@link Mono} if the transform microservice is not
     * running
     */
    protected Mono<String> buildMicroserviceUrl(String transform) {
        // Get a stream of all registered microservices.
        return getTransformMicroServices()
            // Only allow the microservice that performs the given
            // image transform.
            .filter(microservice -> microservice
                    .contains(transform.toLowerCase()))

            // Construct the redirect URL for this transform
            // microservice.
            .map(___ -> baseUrl
                 + transform.toLowerCase()
                 + "/apply-transform?transform="
                 + transform)

            // Return the first (and only) element.
            .next();
    }

    /**
     * Send a POST request to the specified url using a multi-value
     * map that contains the passed image bytes and image file name.
     *
     * @param url        The microservice url
     * @param imageBytes The image bytes to post
     * @param fileName   A file name where returned image will be stored
     * @return A {@link Mono} to a {@link TransformedImage}
     */
    protected Mono<TransformedImage> postRequest(String url,
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

        // Use webClient to post the request to the designated URL
        // (passing the map in the body of the POST request).  Then
        // retrieve the response from the microservice, convert the
        // body to a Mono<TransformedImage> object, and return this
        // object to the caller.

        // TODO -- you fill in here replacing 'return null' with your
        // solution.
        return null;
    }

    /**
     * A factory method that uses the Eureka discovery client to
     * return {@link Flux} containing all microservices.
     *
     * @return A {@link Flux} that emits all registered microservices
     */
    public Flux<String> getTransformMicroServices() {
        // Get a List of all registered microservices and convert it
        // to a Flux.

        // TODO -- you fill in here replacing 'return null' with your
        // solution.
        return null;
    }
}

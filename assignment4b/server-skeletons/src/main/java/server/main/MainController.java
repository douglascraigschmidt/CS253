package server.main;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import server.common.model.TransformedImage;

import java.io.IOException;
import java.util.List;

import static server.common.Constants.EndPoint.APPLY_TRANSFORMS;

/**
 * This Spring controller is the main entry point for remote clients.
 * It demonstrates how Spring can be used to handle an HTTP POST
 * request via reactive programming.  This request applies all the
 * given {@code transforms} and returns a {@link Mono} that emits a
 * {@link List} of {@link TransformedImage} objects.
 *
 * In Spring's approach to building RESTful web services, HTTP
 * requests are handled by a controller (identified by the
 * {@code @RestController} annotation) that defines the endpoints (aka
 * routes) for each supported operation, i.e., {@code @GetMapping},
 * {@code @PostMapping}, {@code @PutMapping}, and
 * {@code @DeleteMapping}, which correspond to the HTTP GET, POST,
 * PUT, and DELETE calls, respectively.
 *
 * Spring uses the {@code @PostMapping} annotation to map HTTP POST
 * requests onto methods in the {@link MainController}.  POST requests
 * invoked from any HTTP web client (e.g., a web browser or Android
 * app) or command-line utility (e.g., Curl or Postman).
 *
 * The {@code @ResponseBody} annotation tells a controller that the
 * object returned is automatically serialized into JSON and passed
 * back within the body of HttpResponse object.
 */
@RestController
@ResponseBody
public class MainController {
    /**
     * This auto-wired field connects the {@link MainController} to
     * the {@link MainService}.
     */
    @Autowired
    MainService mainService;

    /**
     * Apply the given {@link List} of {@code transforms} to the given
     * {@code image} and return a {@link Mono} that emits a {@link
     * List} of {@link TransformedImage} objects.
     *
     * @param transforms A {@link List} of transforms to apply
     * @param image      The contents of the image as a {@link MultipartFile}
     * @param parallel   True if transforms should be applied in
     *                   parallel, else false
     * @return A {@link Mono} that emits a {@link List} of {@link
     * TransformedImage} objects
     */
    // TODO -- you fill in here by adding the appropriate annotations
    // to create a Spring WebFlux endpoint method.
    public Mono<List<TransformedImage>> applyTransforms(
            List<String> transforms,
            MultipartFile image,
            Boolean parallel
    ) throws IOException {
        // Use the 'mainService' field to call a MainService method
        // that applies all the transforms and obtains a Flux stream
        // of transformed images in response.  Collect these results
        // into a Mono that downgrades Flux<TransformedImage> objects
        // to a Mono<List<TransformedImage>> objects for REST
        // compatibility and return this as the result of the method.

        // TODO -- you fill in here replacing 'return null' with your
        // solution.
        return null;
    }
}

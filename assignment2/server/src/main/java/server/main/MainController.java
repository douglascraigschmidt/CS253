package server.main;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import server.common.model.TransformedImage;

import java.io.IOException;
import java.util.List;

import static server.common.Constants.EndPoint.APPLY_TRANSFORMS;

/**
 * This Spring controller is the main entry point for remote clients.
 * It demonstrates how Spring can be used to handle an HTTP POST
 * request via Java parallel streams.  This request applies all the
 * given {@code transforms} and returns a {@link List} of {@link
 * TransformedImage} objects.
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
 * The {@code @RestController} annotation tells a controller that the
 * object returned is automatically serialized into JSON and passed
 * back within the body of an {@link HttpResponse} object.
 */
@RestController
public class MainController {
    /**
     * This auto-wired field connects the {@link MainController} to
     * the {@link MainService}.
     */
    @Autowired
    MainService imageService;

    /**
     * Applies the given {@link List} of {@code transforms} to the
     * given {@code image} and returns a {@link List} of {@link
     * TransformedImage} objects.
     *
     * @param transforms {@link List} of transform names
     * @param image      The contents of the image as a {@link MultipartFile}
     * @param parallel   True if transforms should be applied in
     *                   parallel, else false
     * @return {@link List} of {@link TransformedImage} objects
     */
    // TODO -- you fill in below by adding the appropriate annotations
    // to create a Spring WebMVC endpoint method that calls the service.
    // to apply the transform and return the TransformedImage.
    public List<TransformedImage> applyTransforms(
            List<String> transforms,
            MultipartFile image,
            Boolean parallel
    ) throws IOException {
        // Apply all transforms and return a List of transformed
        // images.

        // TODO -- you fill in here replacing this statement with your
        // solution.
        // SOLUTION-START
        return null;
    }
}

package server.common;

import org.springframework.boot.web.servlet.MultipartConfigFactory;
import org.springframework.cloud.client.loadbalancer.LoadBalanced;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;
import org.springframework.util.unit.DataSize;
import org.springframework.web.client.RestTemplate;

import javax.servlet.MultipartConfigElement;

/**
 * This class contains a {@code Bean} annotation that can be injected
 * into classes using the {@code @Autowired} annotation.
 *
 *  The {@code LoadBalanced} annotation indicates the {@link
 * RestTemplate} should be based on client-side load balancing and
 * checks Eureka server to resolve the service name to host/port.
 */
@Component
public class Components {
    /**
     * Maximum multipart request size.
     */
    private static final int MAX_REQUEST_SIZE_IN_MB = 500;

    /**
     * This factory method returns a new {@link RestTemplate}, which
     * enables a synchronous client to perform HTTP requests.
     * 
     * @return A new {@link RestTemplate}.
     */
    @Bean
    @LoadBalanced
    public RestTemplate getRestTemplate() {
        return new RestTemplate();
    }

    /**
     * This configuration bean adds the ability to accept large size
     * multipart requests into the web container.
     */
    @Bean
    public MultipartConfigElement multipartConfigElement() {
        // Setup the application container to be accept multipart requests
        final MultipartConfigFactory factory = new MultipartConfigFactory();

        // Place upper bounds on the size of the requests to ensure that
        // clients don't abuse the web container by sending huge requests
        factory.setMaxFileSize(DataSize.ofMegabytes(MAX_REQUEST_SIZE_IN_MB));
        factory.setMaxRequestSize(DataSize.ofMegabytes(MAX_REQUEST_SIZE_IN_MB));

        // Return the configuration to setup multipart in the container
        return factory.createMultipartConfig();
    }
}

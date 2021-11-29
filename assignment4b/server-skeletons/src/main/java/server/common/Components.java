package server.common;

import org.springframework.boot.web.servlet.MultipartConfigFactory;
import org.springframework.cloud.client.loadbalancer.LoadBalanced;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;
import org.springframework.util.unit.DataSize;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.reactive.function.client.WebClient;

import javax.servlet.MultipartConfigElement;

/**
 * This class contains Beans that can be injected into classes using
 * the {@code @Autowired} annotation.
 */
@Component
public class Components {
    /**
     * Maximum multipart request size.
     */
    private static final int MAX_REQUEST_SIZE_IN_MB = 500;

    /**
     * This factory method returns a new {@link WebClient.Builder()}.
     *
     * @return A new {@link WebClient.Builder()}
     */
    @Bean
    @LoadBalanced
    WebClient.Builder builder() {
        return WebClient.builder();
    }

    /**
     * This factory method returns an initialized {@link WebClient}.
     *
     * @param builder A builder used to create a new {@link WebClient}
     * @return An initialized {@link WebClient}
     */
    @Bean
    WebClient webClient(WebClient.Builder builder) {
        return builder
            .codecs(configurer -> configurer
                    .defaultCodecs()
                    .maxInMemorySize(16 * 1024 * 1024))
            .build();
    }

    /**
     * @return An initialized {@link RestTemplate}
     */
    @Bean
    @LoadBalanced
    public RestTemplate getRestTemplate() {
        return new RestTemplate();
    }

    /**
     * Adds the ability to accept large size multipart requests into
     * the web container.
     */
    @Bean
    public MultipartConfigElement multipartConfigElement() {
        // Setup the application container to be accept multipart
        // requests.
        final MultipartConfigFactory factory = new MultipartConfigFactory();

        // Place upper bounds on the size of the requests to ensure
        // that clients don't abuse the web container by sending huge
        // requests.
        factory.setMaxFileSize(DataSize.ofMegabytes(MAX_REQUEST_SIZE_IN_MB));
        factory.setMaxRequestSize(DataSize.ofMegabytes(MAX_REQUEST_SIZE_IN_MB));

        // Return the configuration to setup multipart in the
        // container.
        return factory.createMultipartConfig();
    }

    //    @Configuration
    //    @EnableWebFlux
    //    public class WebfluxConfig implements WebFluxConfigurer {
    //        @Override
    //        public void configureHttpMessageCodecs(ServerCodecConfigurer configurer) {
    //            configurer.defaultCodecs().maxInMemorySize(262114);
    //        }
    //    }
}

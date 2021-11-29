package server.microservices.tint;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.PropertySource;
import org.springframework.context.annotation.PropertySources;

import server.common.Components;
import server.common.Constants;
import server.microservices.common.TransformController;

/**
 * This class provides the entry point for the tint microservice.
 * <p>
 * The {@code @SpringBootApplication} annotation enables apps to use
 * auto-configuration, component scan, and to define extra
 * configurations on their "application" class.
 * <p>
 * The {@code @EnableDiscoveryClient} annotation enables service
 * registration and discovery, i.e., this process registers itself
 * with the discovery-server service using its application name.
 * <p>
 * The {@code @ComponentScan} annotation tells Spring the packages to
 * scan for annotated components (i.e., tagged with @Component).
 * <p>
 * The {@code @PropertySources} annotation is used to provide
 * properties files to Spring Environment.
 */
@SpringBootApplication
@EnableDiscoveryClient
@ComponentScan(basePackageClasses = {
        TintMicroservice.class,
        Components.class,
        TransformController.class})
@PropertySources({
        @PropertySource(Constants.Resources.EUREKA_CLIENT_PROPERTIES),
        @PropertySource("classpath:/microservices/tint-microservice.properties")})
public class TintMicroservice {
    /**
     * A static main() entry point is needed to run this transform
     * microservice.
     */
    public static void main(String[] args) {
        // Launch this microservice through Spring Boot.
        SpringApplication.run(TintMicroservice.class);
    }
}

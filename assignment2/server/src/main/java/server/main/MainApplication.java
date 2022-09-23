package server.main;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.PropertySource;
import server.common.Components;

/**
 * This application is the "front-end" app gateway that forwards
 * incoming HTTP requests from clients to microservices in the Image
 * Crawler app.  These requests apply all the given {@code transforms}
 * and return a list of TransformedImage objects.  This class also plays
 * the role of a Eureka "client" wrt microservice discovery.
 *
 * The {@code @SpringBootApplication} annotation enables apps to use
 * auto-configuration, component scan, and to define extra
 * configurations on their "application" class.
 *
 * The {@code @EnableDiscoveryClient} annotation enables service
 * registration and discovery.  However, this app uses a fixed port
 * specified in the resources {@code application.properties} file and
 * does not need to register itself with the eureka discovery client,
 * so {@code autoRegister} is set to false.
 *
 * The {@code @EntityScan} annotation is used when entity classes are
 * not placed in the main application package or its sub-packages. 
 *
 * The {@code @ComponentScan} annotation tells Spring the packages to
 * scan for annotated components (i.e., tagged with
 * {@code @Component}). The {@code @PropertySource} annotation is used
 * to provide a properties file to the Spring Environment.
 */
@SpringBootApplication
@EnableDiscoveryClient(autoRegister = false)
@ComponentScan(basePackageClasses = {
        Components.class,
        MainController.class,
        MainService.class})
@PropertySource("classpath:/main/main-application.properties")
public class MainApplication {
    /**
     * A static main() entry point is needed to run the Flight
     * microservice.
     */
    public static void main(String[] args) {
        // Launch this app gateway through Spring Boot.
        SpringApplication.run(MainApplication.class, args);
    }
}

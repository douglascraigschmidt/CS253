package eureka;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.DiscoveryClient;
import org.springframework.cloud.netflix.eureka.server.EnableEurekaServer;
import org.springframework.context.annotation.ComponentScan;


/**
 * Eureka is a REST (Representational State Transfer) based service
 * that is primarily used in the AWS cloud for locating services
 * for the purpose of load balancing and fail-over of middle-tier
 * servers.
 */
@SpringBootApplication
@EnableEurekaServer
@ComponentScan
public class EurekaApplication {
    @Autowired
    private DiscoveryClient discoveryClient;

    public static void main(String[] args) {
        SpringApplication.run(EurekaApplication.class, args);
    }
}

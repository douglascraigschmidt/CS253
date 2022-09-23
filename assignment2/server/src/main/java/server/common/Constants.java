package server.common;

/**
 * Static class used to centralize all constants used by this project.
 *
 * All HTTP requests URLs use the following convention:
 *
 * http:port//service/endpoint/
 *
 * where "service" is one of the named microservices in the {@link
 * Service} inner class, "endpoint" is one of the endpoints declared
 * in the {@link EndPoint} inner class.
 */
public class Constants {
    /**
     * Each microservice will automatically register itself with the
     * Eureka service using its unique {@link Service} name string and
     * a randomly generated port. HTTP requests can then use the
     * microservice name instead of an IP address and port number.
     *
     * When the a microservice starts up Spring allocates an unused
     * random port (signaled by the {@code server.port=0} entry in the
     * {@code application.properties} file). This port is then used
     * when registering the microservice with the Eureka discovery
     * service (invoked by the {@code @EnableDiscoveryClient}
     * application annotation). Once registered, all requests to
     * microservices can simply use their registered application
     * names.
     *
     * For example, if the tint microservice is allocated port 40928
     * then a tint transform request URL will be mapped by the Eureka
     * server as follows:
     *
     * http://tint/transform -> 192.168.7.23:40928/transform
     */
    public static class Service {
        /**
         * All service names must match the spring.application.name
         * property in each microservice application properties
         * resource file.
         */
        public static final String GRAYSCALE_TRANSFORM = "GrayScaleTransform";
        public static final String SEPIA_TRANSFORM = "SepiaTransform";
        public static final String TINT_TRANSFORM = "TintTransform";
    }

    /**
     * All supported HTTP request endpoints.
     */
    public static class EndPoint {
        public static final String APPLY_TRANSFORMS = "apply-transforms";
        public static final String APPLY_TRANSFORM = "apply-transform";
    }

    /**
     * Common resource file names used by all microservices,
     * which reside in the {@code src/main/resources} folder.
     */
    public static class Resources {
        public static final String EUREKA_CLIENT_PROPERTIES =
                "classpath:/common/eureka-client.properties";
    }
}

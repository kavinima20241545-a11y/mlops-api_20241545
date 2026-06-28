package com.mlops.application;

import org.glassfish.grizzly.http.server.HttpServer;
import org.glassfish.jersey.grizzly2.httpserver.GrizzlyHttpServerFactory;
import org.glassfish.jersey.jackson.JacksonFeature;
import org.glassfish.jersey.server.ResourceConfig;

import java.io.IOException;
import java.net.URI;
import java.util.logging.Logger;

/**
 *main class
 * Server listens to http://localhost:8080/api/v1
 */
public class Main {

    private static final Logger LOG = Logger.getLogger(Main.class.getName());
    public static final String BASE_URI = "http://0.0.0.0:8080/";

    public static void main(String[] args) throws IOException {
        // Scan the whole com.mlops package for @Path, @Provider classes
        final ResourceConfig config = new ResourceConfig()
                .packages("com.mlops")
                .register(JacksonFeature.class);  // Jackson JSON serialization

        final HttpServer server = GrizzlyHttpServerFactory.createHttpServer(
                URI.create(BASE_URI), config);

        LOG.info("MLOps API running at http://localhost:8080/api/v1");
        LOG.info("Press ENTER to stop...");
        System.in.read();
        server.shutdownNow();
    }
}

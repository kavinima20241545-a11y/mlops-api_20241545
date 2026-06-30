package com.mlops.application;

import org.glassfish.grizzly.http.server.HttpServer;
import org.glassfish.jersey.grizzly2.httpserver.GrizzlyHttpServerFactory;
import org.glassfish.jersey.jackson.JacksonFeature;
import org.glassfish.jersey.server.ResourceConfig;

import java.io.IOException;
import java.net.URI;
import java.util.logging.Logger;

public class Main {

    private static final Logger LOG = Logger.getLogger(Main.class.getName());

    public static void main(String[] args) throws IOException {

        ResourceConfig config = new ResourceConfig();
        config.packages("com.mlops.resource", "com.mlops.exception", "com.mlops.filter");
        config.register(JacksonFeature.class);

        URI baseUri = URI.create("http://0.0.0.0:8080/api/v1/");

        HttpServer server = GrizzlyHttpServerFactory.createHttpServer(baseUri, config);

        LOG.info("=== Server started: http://localhost:8080/api/v1/ ===");
        LOG.info("Press ENTER to stop...");
        System.in.read();
        server.shutdownNow();
    }
}
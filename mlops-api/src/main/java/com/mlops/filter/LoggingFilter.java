package com.mlops.filter;

import javax.ws.rs.container.*;
import javax.ws.rs.ext.Provider;
import java.io.IOException;
import java.util.logging.Logger;

/**
 LoggingFilter implements ContainerRequestFilter
 */
@Provider
public class LoggingFilter implements ContainerRequestFilter, ContainerResponseFilter {

    private static final Logger LOG = Logger.getLogger(LoggingFilter.class.getName());

    // Incoming request
    @Override
    public void filter(ContainerRequestContext req) throws IOException {

        LOG.info(String.format("[REQUEST]  %s %s | Accept: %s",
                req.getMethod(),
                req.getUriInfo().getRequestUri(),
                req.getHeaderString("Accept")));
    }

    // Outgoing response
    @Override
    public void filter(ContainerRequestContext req,
                       ContainerResponseContext res) throws IOException {

        LOG.info(String.format("[RESPONSE] %s %s | Status: %d | Content-Type: %s",
                req.getMethod(),
                req.getUriInfo().getRequestUri(),
                res.getStatus(),
                res.getMediaType()));
    }
}

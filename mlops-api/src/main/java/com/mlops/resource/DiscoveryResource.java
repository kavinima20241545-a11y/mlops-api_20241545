package com.mlops.resource;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.HashMap;
import java.util.Map;

/**
 DiscoveryResource class
 GET /api/v1  - > returns API metadata.
 */
@Path("/")
@Produces(MediaType.APPLICATION_JSON)
public class DiscoveryResource {

    @GET
    public Response discover() {
        Map<String, Object> meta = new HashMap<>();
        meta.put("name", "MLOps Pipeline Management API");
        meta.put("version", "1.0.0");
        meta.put("description", "Manages ML Workspaces and Models for an AI research lab.");
        meta.put("contact", "admin@mlops-lab.ai");

        Map<String, String> resources = new HashMap<>();
        resources.put("workspaces", "/api/v1/workspaces");
        resources.put("models",     "/api/v1/models");
        meta.put("resources", resources);

        return Response.ok(meta).build();
    }
}

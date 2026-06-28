package com.mlops.resource;

import com.mlops.exception.LinkedWorkspaceNotFoundException;
import com.mlops.exception.ModelNotFoundException;
import com.mlops.model.DataStore;
import com.mlops.model.MachineLearningModel;
import com.mlops.model.MLWorkspace;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 Part 3 – Model Operations.
 */
@Path("/models")
@Produces(MediaType.APPLICATION_JSON)  // Part 4.1: class-level @Produces avoids
@Consumes(MediaType.APPLICATION_JSON)  // repeating the annotation on every method
public class ModelResource {

    private final DataStore store = DataStore.getInstance();

    // GET
    @GET
    public Response getAllModels(@QueryParam("status") String status) {
        List<MachineLearningModel> all =
                store.getModels().values().stream().collect(Collectors.toList());

        if (status != null && !status.isBlank()) {
            all = all.stream()
                    .filter(m -> m.getStatus().equalsIgnoreCase(status))
                    .collect(Collectors.toList());
        }
        return Response.ok(all).build();
    }

    // POST
    @POST
    public Response createModel(MachineLearningModel incoming) {
        // Part 3.1 integrity: verify the linked workspace really exists
        String wsId = incoming.getWorkspaceId();
        MLWorkspace workspace = store.findWorkspace(wsId)
                .orElseThrow(() -> new LinkedWorkspaceNotFoundException(wsId));

        // Server-generated ID (security + integrity – see report answer)
        String newId = "MOD-" + UUID.randomUUID().toString().substring(0, 4).toUpperCase();
        incoming.setId(newId);

        store.saveModel(incoming);

        // Keep workspace's modelId list in sync
        workspace.getModelIds().add(newId);
        store.saveWorkspace(workspace);

        return Response.status(Response.Status.CREATED).entity(incoming).build();
    }

    // GET
    @GET
    @Path("/{modelId}")
    public Response getModel(@PathParam("modelId") String modelId) {
        MachineLearningModel model = store.findModel(modelId)
                .orElseThrow(() -> new ModelNotFoundException(modelId));
        return Response.ok(model).build();
    }

    // Sub-resource locator
    // JAX-RS runtime delegates further path matching to EvaluationMetricResource.
    @Path("/{modelId}/metrics")
    public EvaluationMetricResource getMetricResource(
            @PathParam("modelId") String modelId) {
        return new EvaluationMetricResource(modelId);
    }
}

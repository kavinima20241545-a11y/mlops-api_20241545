package com.mlops.resource;

import com.mlops.exception.ModelDeprecatedException;
import com.mlops.exception.ModelNotFoundException;
import com.mlops.model.DataStore;
import com.mlops.model.EvaluationMetric;
import com.mlops.model.MachineLearningModel;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.List;
import java.util.UUID;

/**
 EvaluationMetric sub-resource.
 */
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class EvaluationMetricResource {

    private final String modelId;
    private final DataStore store = DataStore.getInstance();

    public EvaluationMetricResource(String modelId) {
        this.modelId = modelId;
    }

    // GET
    @GET
    public Response getMetrics() {
        // Ensure model exists
        store.findModel(modelId)
                .orElseThrow(() -> new ModelNotFoundException(modelId));

        List<EvaluationMetric> history = store.getMetricsForModel(modelId);
        return Response.ok(history).build();
    }

    // POST
    @POST
    public Response addMetric(EvaluationMetric incoming) {
        MachineLearningModel model = store.findModel(modelId)
                .orElseThrow(() -> new ModelNotFoundException(modelId));

        //  Deprecated models cannot receive metrics
        if ("DEPRECATED".equalsIgnoreCase(model.getStatus())) {
            throw new ModelDeprecatedException(modelId);
        }

        // Server generates ID and timestamp if not provided
        if (incoming.getId() == null || incoming.getId().isBlank()) {
            incoming.setId(UUID.randomUUID().toString());
        }
        if (incoming.getTimestamp() == 0) {
            incoming.setTimestamp(System.currentTimeMillis());
        }

        store.addMetric(modelId, incoming);

        // Update parent model's latestAccuracy
        model.setLatestAccuracy(incoming.getAccuracyScore());
        store.saveModel(model);

        return Response.status(Response.Status.CREATED).entity(incoming).build();
    }
}

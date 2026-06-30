package com.mlops.resource;

import com.mlops.exception.WorkspaceNotFoundException;
import com.mlops.exception.WorkspaceNotEmptyException;
import com.mlops.model.DataStore;
import com.mlops.model.MLWorkspace;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 Workspace Management
 */
@Path("workspaces")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class WorkspaceResource {

    private final DataStore store = DataStore.getInstance();

    // GET
    @GET
    public Response getAllWorkspaces() {
        List<MLWorkspace> list = new ArrayList<>(store.getWorkspaces().values());
        return Response.ok(list)
                // Cache-Control
                // the list for 60 seconds, reducing repeated server calls.
                .header("Cache-Control", "public, max-age=60")
                .build();
    }

    // POST
    @POST
    public Response createWorkspace(MLWorkspace incoming) {
        // Server generates ID – prevents client-controlled identity injection
        String newId = "WS-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
        incoming.setId(newId);
        store.saveWorkspace(incoming);
        return Response.status(Response.Status.CREATED).entity(incoming).build();
    }

    // GET
    @GET
    @Path("/{workspaceId}")
    public Response getWorkspace(@PathParam("workspaceId") String workspaceId) {
        MLWorkspace ws = store.findWorkspace(workspaceId)
                .orElseThrow(() -> new WorkspaceNotFoundException(workspaceId));
        return Response.ok(ws).build();
    }

    // HEAD
    @HEAD
    @Path("/{workspaceId}")
    public Response headWorkspace(@PathParam("workspaceId") String workspaceId) {
        store.findWorkspace(workspaceId)
                .orElseThrow(() -> new WorkspaceNotFoundException(workspaceId));
        return Response.ok().build();  // 200, no body
    }

    // DELETE
    @DELETE
    @Path("/{workspaceId}")
    public Response deleteWorkspace(@PathParam("workspaceId") String workspaceId) {
        MLWorkspace ws = store.findWorkspace(workspaceId)
                .orElseThrow(() -> new WorkspaceNotFoundException(workspaceId));

        // Block deletion if models are still linked
        if (!ws.getModelIds().isEmpty()) {
            throw new WorkspaceNotEmptyException(workspaceId);
        }

        store.deleteWorkspace(workspaceId);

        return Response.noContent().build();  // 204 No Content
    }
}

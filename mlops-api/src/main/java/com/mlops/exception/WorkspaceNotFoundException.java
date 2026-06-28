package com.mlops.exception;

public class WorkspaceNotFoundException extends RuntimeException {
    public WorkspaceNotFoundException(String workspaceId) {
        super("Workspace not found: " + workspaceId);
    }
}

package com.mlops.exception;

public class WorkspaceNotEmptyException extends RuntimeException {
    public WorkspaceNotEmptyException(String workspaceId) {
        super("Workspace '" + workspaceId + "' still has models assigned to it and cannot be deleted.");
    }
}

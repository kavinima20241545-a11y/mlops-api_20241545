package com.mlops.exception;

public class LinkedWorkspaceNotFoundException extends RuntimeException {
    public LinkedWorkspaceNotFoundException(String workspaceId) {
        super("Cannot register model: workspace '" + workspaceId + "' does not exist.");
    }
}

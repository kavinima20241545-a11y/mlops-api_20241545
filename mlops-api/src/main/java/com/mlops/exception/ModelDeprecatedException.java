package com.mlops.exception;

public class ModelDeprecatedException extends RuntimeException {
    public ModelDeprecatedException(String modelId) {
        super("Model '" + modelId + "' is DEPRECATED and cannot accept new evaluation metrics.");
    }
}

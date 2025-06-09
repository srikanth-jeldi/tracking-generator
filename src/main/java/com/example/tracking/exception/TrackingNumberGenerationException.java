package com.example.tracking.exception;

public class TrackingNumberGenerationException extends RuntimeException {
    public TrackingNumberGenerationException(String message) {
        super(message);
    }

    public TrackingNumberGenerationException(String message, Throwable cause) {
        super(message, cause);
    }
}
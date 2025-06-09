package com.example.tracking.exception;

public class InvalidTrackingRequestException extends RuntimeException {
    public InvalidTrackingRequestException(String message) {
        super(message);
    }

    public InvalidTrackingRequestException(String message, Throwable cause) {
        super(message, cause);
    }
}
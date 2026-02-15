package com.example.logicservice.dto;

import java.time.Instant;

public class ErrorResponse {
    private String error;
    private String message;
    private Instant timestamp;

    public ErrorResponse() {
    }

    public ErrorResponse(String error, String message, Instant timestamp) {
        this.error = error;
        this.message = message;
        this.timestamp = timestamp;
    }

    public String getError() {
        return error;
    }

    public void setError(String error) {
        this.error = error;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public Instant getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Instant timestamp) {
        this.timestamp = timestamp;
    }
}

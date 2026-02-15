package com.game.user_service.exception;

import java.time.Instant;

public class ErrorResponse {
    private String message;
    private Instant timestamp;

    public ErrorResponse(String message, Instant timestamp) {
        this.message = message;
        this.timestamp = timestamp;
    }

    public String getMessage() {
        return message;
    }

    public Instant getTimestamp() {
        return timestamp;
    }
}

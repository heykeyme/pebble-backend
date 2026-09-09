package com.todo.common.errors;

import java.time.Instant;

// Shared error envelope so every service reports failures the same way.
public record ApiError(int status, String error, String message, Instant timestamp) {

    public static ApiError of(int status, String error, String message) {
        return new ApiError(status, error, message, Instant.now());
    }
}

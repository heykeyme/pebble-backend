package com.todo.common.dtos;

// Shared success envelope so every service returns the same response shape.
public record ApiResponse<T>(boolean success, T data) {

    public static <T> ApiResponse<T> ok(T data) {
        return new ApiResponse<>(true, data);
    }
}

package com.menstyle.dto.response;

import java.time.Instant;

public record ApiResponse<T>(
        boolean success,
        String message,
        T data,
        Instant timestamp,
        Integer statusCode,
        String path
) {

    public static <T> ApiResponse<T> success(String message, T data, int statusCode, String path) {
        return new ApiResponse<>(true, message, data, Instant.now(), statusCode, path);
    }

    public static <T> ApiResponse<T> error(String message, T data, int statusCode, String path) {
        return new ApiResponse<>(false, message, data, Instant.now(), statusCode, path);
    }
}

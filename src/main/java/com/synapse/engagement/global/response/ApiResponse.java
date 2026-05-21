package com.synapse.engagement.global.response;

public record ApiResponse<T>(
        boolean success,
        T data,
        String code,
        String message) {

    public static <T> ApiResponse<T> success(T data) {
        return new ApiResponse<>(true, data, null, null);
    }

    public static <T> ApiResponse<T> error(String code, String message) {
        return new ApiResponse<>(false, null, code, message);
    }
}

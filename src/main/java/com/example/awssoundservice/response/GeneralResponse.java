package com.example.awssoundservice.response;

public record GeneralResponse<T>(
        Integer statusCode,
        String message,
        T data
) {

    public static <T> GeneralResponse<T> successResponse(T data) {
        return new GeneralResponse<>(200, "OK", data);
    }

}

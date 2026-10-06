package com.abs.user.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class ApiEnvelope<T> {

    private final boolean success;
    private final T data;
    private final ApiError error;

    public static <T> ApiEnvelope<T> success(T data) {
        return new ApiEnvelope<>(true, data, null);
    }

    public static <T> ApiEnvelope<T> failure(ApiError error) {
        return new ApiEnvelope<>(false, null, error);
    }
}
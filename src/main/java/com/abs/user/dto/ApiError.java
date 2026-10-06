package com.abs.user.dto;

import java.time.Instant;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@AllArgsConstructor
public class ApiError {

    private final Instant timestamp;
    private final int status;
    private final String errorCode;
    private final String message;
}
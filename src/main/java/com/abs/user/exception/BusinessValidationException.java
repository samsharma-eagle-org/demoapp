package com.abs.user.exception;

import lombok.Getter;

@Getter
public class BusinessValidationException extends RuntimeException {

    private final String errorCode;

    public BusinessValidationException(String errorCode, String message) {
        super(message);
        this.errorCode = errorCode;
    }
}
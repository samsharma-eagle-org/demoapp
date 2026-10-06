package com.abs.user.exception;

import java.time.Instant;

import com.abs.user.dto.ApiEnvelope;
import com.abs.user.dto.ApiError;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.NoHandlerFoundException;

@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiEnvelope<Void>> handleValidation(MethodArgumentNotValidException exception) {
        String message = exception.getBindingResult().getFieldErrors().stream()
                .findFirst()
                .map(error -> error.getField() + ": " + error.getDefaultMessage())
                .orElse("Request validation failed.");
            log.warn("event=request_validation_failed error_code=VALIDATION_ERROR");
        return error(HttpStatus.BAD_REQUEST, "VALIDATION_ERROR", message);
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ApiEnvelope<Void>> handleUnreadableRequest(HttpMessageNotReadableException exception) {
        log.warn("event=unreadable_request error_code=INVALID_REQUEST");
        return error(HttpStatus.BAD_REQUEST, "INVALID_REQUEST", "The request body is missing or malformed.");
    }

    @ExceptionHandler(BusinessValidationException.class)
    public ResponseEntity<ApiEnvelope<Void>> handleBusinessValidation(BusinessValidationException exception) {
        log.warn("event=business_validation_failed error_code={}", exception.getErrorCode());
        return error(HttpStatus.BAD_REQUEST, exception.getErrorCode(), exception.getMessage());
    }

    @ExceptionHandler(NoHandlerFoundException.class)
    public ResponseEntity<ApiEnvelope<Void>> handleNoHandlerFound(NoHandlerFoundException exception) {
        log.warn("event=route_not_found error_code=RESOURCE_NOT_FOUND");
        return error(HttpStatus.NOT_FOUND, "RESOURCE_NOT_FOUND", "No matching endpoint was found.");
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiEnvelope<Void>> handleUnexpected(Exception exception) {
        log.error("event=unhandled_exception error_code=INTERNAL_SERVER_ERROR", exception);
        return error(HttpStatus.INTERNAL_SERVER_ERROR, "INTERNAL_SERVER_ERROR", "An unexpected error occurred.");
    }

    private ResponseEntity<ApiEnvelope<Void>> error(HttpStatus status, String errorCode, String message) {
        ApiError apiError = ApiError.builder()
                .timestamp(Instant.now())
                .status(status.value())
                .errorCode(errorCode)
                .message(message)
                .build();
        return ResponseEntity.status(status).body(ApiEnvelope.failure(apiError));
    }
}
package com.abs.user.controller;

import javax.validation.Valid;

import com.abs.user.dto.ApiEnvelope;
import com.abs.user.dto.UserRegistrationRequest;
import com.abs.user.dto.UserResponse;
import com.abs.user.service.UserRegistrationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;

@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
public class UserController {

    private final UserRegistrationService userRegistrationService;

    @PostMapping
    @Operation(summary = "Register a user", description = "Creates a user profile with a generated employee ID and email address.")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "User profile created",
                    content = @Content(schema = @Schema(implementation = ApiEnvelope.class))),
            @ApiResponse(responseCode = "400", description = "Request validation failed"),
            @ApiResponse(responseCode = "500", description = "Unexpected service failure")
    })
    public ResponseEntity<ApiEnvelope<UserResponse>> register(
            @Valid @RequestBody UserRegistrationRequest request) {
        UserResponse response = userRegistrationService.register(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiEnvelope.success(response));
    }
}
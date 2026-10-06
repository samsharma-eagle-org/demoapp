package com.abs.user.dto;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@Schema(description = "Names required to create a user profile")
public class UserRegistrationRequest {

    @NotBlank(message = "must not be blank")
    @Size(max = 100, message = "must be at most 100 characters")
    @Schema(required = true, maxLength = 100)
    private String firstName;

    @NotBlank(message = "must not be blank")
    @Size(max = 100, message = "must be at most 100 characters")
    @Schema(required = true, maxLength = 100)
    private String lastName;
}
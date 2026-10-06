package com.abs.user.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "Public user profile; excludes the internal database ID")
public class UserResponse {

    @Schema(required = true, pattern = "^[0-9]{5}$")
    private String userId;

    @Schema(required = true)
    private String firstName;

    @Schema(required = true)
    private String lastName;

    @Schema(required = true, format = "email")
    private String emailId;

    @Schema(required = true, defaultValue = "true")
    private Boolean active;
}
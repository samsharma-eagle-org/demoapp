package com.abs.user.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.abs.user.dto.UserRegistrationRequest;
import com.abs.user.dto.UserResponse;
import com.abs.user.service.UserRegistrationService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(UserController.class)
class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private UserRegistrationService userRegistrationService;

    @Test
    void createsUserAndReturnsOnlyPublicResponseFields() throws Exception {
        when(userRegistrationService.register(any(UserRegistrationRequest.class)))
                .thenReturn(UserResponse.builder()
                        .userId("48291")
                        .firstName("Ada")
                        .lastName("Lovelace")
                        .emailId("ada.lovelace@abs.com")
                        .active(true)
                        .build());

        mockMvc.perform(post("/api/v1/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"firstName\":\"Ada\",\"lastName\":\"Lovelace\"}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.userId").value("48291"))
                .andExpect(jsonPath("$.data.firstName").value("Ada"))
                .andExpect(jsonPath("$.data.lastName").value("Lovelace"))
                .andExpect(jsonPath("$.data.emailId").value("ada.lovelace@abs.com"))
                .andExpect(jsonPath("$.data.active").value(true))
                .andExpect(jsonPath("$.data.id").doesNotExist());
    }

            @ParameterizedTest
            @ValueSource(strings = {
                "{\"lastName\":\"Lovelace\"}",
                "{\"firstName\":null,\"lastName\":\"Lovelace\"}",
                "{\"firstName\":\"\",\"lastName\":\"Lovelace\"}",
                "{\"firstName\":\"   \",\"lastName\":\"Lovelace\"}",
                "{\"firstName\":\"Ada\"}",
                "{\"firstName\":\"Ada\",\"lastName\":null}",
                "{\"firstName\":\"Ada\",\"lastName\":\"\"}",
                "{\"firstName\":\"Ada\",\"lastName\":\"   \"}"
            })
            void rejectsMissingNullEmptyAndBlankNames(String body) throws Exception {
            mockMvc.perform(post("/api/v1/users")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(body))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.data").doesNotExist())
                .andExpect(jsonPath("$.error.timestamp").exists())
                .andExpect(jsonPath("$.error.status").value(400))
                .andExpect(jsonPath("$.error.errorCode").value("VALIDATION_ERROR"))
                .andExpect(jsonPath("$.error.message").isNotEmpty())
                .andExpect(jsonPath("$.error.id").doesNotExist());
            }

            @Test
            void rejectsNameThatNormalizesToEmpty() throws Exception {
            when(userRegistrationService.register(any(UserRegistrationRequest.class)))
                .thenThrow(new com.abs.user.exception.BusinessValidationException(
                    "INVALID_NAME_FOR_EMAIL", "Each name must contain a letter or digit."));

            mockMvc.perform(post("/api/v1/users")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content("{\"firstName\":\"!!!\",\"lastName\":\"Lovelace\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error.errorCode").value("INVALID_NAME_FOR_EMAIL"))
                .andExpect(jsonPath("$.data.id").doesNotExist());
            }

            @Test
            void mapsUnexpectedFailuresToStandardErrorEnvelope() throws Exception {
            when(userRegistrationService.register(any(UserRegistrationRequest.class)))
                .thenThrow(new IllegalStateException("internal details"));

            mockMvc.perform(post("/api/v1/users")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content("{\"firstName\":\"Ada\",\"lastName\":\"Lovelace\"}"))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.data").doesNotExist())
                .andExpect(jsonPath("$.error.status").value(500))
                .andExpect(jsonPath("$.error.errorCode").value("INTERNAL_SERVER_ERROR"))
                .andExpect(jsonPath("$.error.message").value("An unexpected error occurred."))
                .andExpect(jsonPath("$.error.message").value(org.hamcrest.Matchers.not("internal details")));
            }

            @Test
            void mapsUnmatchedRouteToStandardNotFoundEnvelope() throws Exception {
            mockMvc.perform(get("/api/v1/missing"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.data").doesNotExist())
                .andExpect(jsonPath("$.error.status").value(404))
                .andExpect(jsonPath("$.error.errorCode").value("RESOURCE_NOT_FOUND"));
            }

    @Test
    void mapsMalformedJsonToStandardErrorEnvelope() throws Exception {
        mockMvc.perform(post("/api/v1/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{invalid-json"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.error.errorCode").value("INVALID_REQUEST"))
                .andExpect(jsonPath("$.error.status").value(400));
    }
}
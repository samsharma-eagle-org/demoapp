package com.abs.user.dto;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.Instant;
import org.junit.jupiter.api.Test;

class UserDtoTest {

    @Test
    void requestSupportsNoArgsConstructorSettersAndGetters() {
        UserRegistrationRequest request = new UserRegistrationRequest();
        request.setFirstName("Ada");
        request.setLastName("Lovelace");

        assertEquals("Ada", request.getFirstName());
        assertEquals("Lovelace", request.getLastName());
    }

    @Test
    void responseBuilderAndGettersExposeOnlyPublicProfileFields() {
        UserResponse noArgs = new UserResponse();
        noArgs.setUserId("38291");
        noArgs.setFirstName("Katherine");
        noArgs.setLastName("Johnson");
        noArgs.setEmailId("katherine.johnson@abs.com");
        noArgs.setActive(true);
        assertEquals("38291", noArgs.getUserId());

        UserResponse response = UserResponse.builder()
                .userId("48291")
                .firstName("Ada")
                .lastName("Lovelace")
                .emailId("ada.lovelace@abs.com")
                .active(true)
                .build();

        assertEquals("48291", response.getUserId());
        assertEquals("Ada", response.getFirstName());
        assertEquals("Lovelace", response.getLastName());
        assertEquals("ada.lovelace@abs.com", response.getEmailId());
        assertTrue(response.getActive());
        assertNotNull(UserResponse.builder().toString());

        UserResponse allArgs = new UserResponse("58291", "Grace", "Hopper", "grace.hopper@abs.com", false);
        assertEquals("58291", allArgs.getUserId());
        assertEquals("Grace", allArgs.getFirstName());
        assertEquals("Hopper", allArgs.getLastName());
        assertEquals("grace.hopper@abs.com", allArgs.getEmailId());
        assertFalse(allArgs.getActive());

        allArgs.setUserId("68291");
        allArgs.setFirstName("Katherine");
        allArgs.setLastName("Johnson");
        allArgs.setEmailId("katherine.johnson@abs.com");
        allArgs.setActive(true);
        assertEquals("68291", allArgs.getUserId());
        assertEquals("Katherine", allArgs.getFirstName());
        assertEquals("Johnson", allArgs.getLastName());
        assertEquals("katherine.johnson@abs.com", allArgs.getEmailId());
        assertTrue(allArgs.getActive());
    }

    @Test
    void envelopesRepresentSuccessAndFailure() {
        UserResponse data = UserResponse.builder()
                .userId("48291")
                .firstName("Ada")
                .lastName("Lovelace")
                .emailId("ada.lovelace@abs.com")
                .active(true)
                .build();
        ApiEnvelope<UserResponse> success = ApiEnvelope.success(data);

        assertTrue(success.isSuccess());
        assertEquals(data, success.getData());
        assertNull(success.getError());

        ApiError error = ApiError.builder()
                .timestamp(Instant.parse("2026-10-05T00:00:00Z"))
                .status(400)
                .errorCode("VALIDATION_ERROR")
                .message("firstName: must not be blank")
                .build();
            assertNotNull(ApiError.builder().toString());
        ApiEnvelope<Void> failure = ApiEnvelope.failure(error);

        assertFalse(failure.isSuccess());
        assertNull(failure.getData());
        assertEquals(error, failure.getError());
    }
}
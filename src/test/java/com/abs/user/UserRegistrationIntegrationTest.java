package com.abs.user;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.abs.user.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class UserRegistrationIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ObjectMapper objectMapper;

    @BeforeEach
    void clearUsers() {
        userRepository.deleteAll();
    }

    @Test
    void persistsValidUserAndDoesNotExposeDatabaseId() throws Exception {
        mockMvc.perform(post("/api/v1/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"firstName\":\"Ada\",\"lastName\":\"Lovelace\"}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.userId").value(org.hamcrest.Matchers.matchesPattern("[0-9]{5}")))
                .andExpect(jsonPath("$.data.emailId").value("ada.lovelace@abs.com"))
                .andExpect(jsonPath("$.data.active").value(true))
                .andExpect(jsonPath("$.data.id").doesNotExist());

        org.junit.jupiter.api.Assertions.assertEquals(1, userRepository.count());
    }

    @Test
    void invalidNamesReturnErrorEnvelopeWithoutPersistingUser() throws Exception {
        mockMvc.perform(post("/api/v1/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"firstName\":\"Ada\",\"lastName\":\"   \"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.data").doesNotExist())
                .andExpect(jsonPath("$.error.timestamp").exists())
                .andExpect(jsonPath("$.error.status").value(400))
                .andExpect(jsonPath("$.error.errorCode").value("VALIDATION_ERROR"));

        org.junit.jupiter.api.Assertions.assertEquals(0, userRepository.count());
    }

    @Test
    void normalizedEmptyNameReturnsBusinessErrorWithoutPersistingUser() throws Exception {
        mockMvc.perform(post("/api/v1/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"firstName\":\"!!!\",\"lastName\":\"Lovelace\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error.errorCode").value("INVALID_NAME_FOR_EMAIL"));

        org.junit.jupiter.api.Assertions.assertEquals(0, userRepository.count());
    }

    @Test
    void repeatedNamesReceiveProgressivelyUniqueEmailAddresses() throws Exception {
        mockMvc.perform(post("/api/v1/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"firstName\":\"Ada\",\"lastName\":\"Lovelace\"}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.emailId").value("ada.lovelace@abs.com"));

        mockMvc.perform(post("/api/v1/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"firstName\":\"Ada\",\"lastName\":\"Lovelace\"}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.emailId").value("ada.a.lovelace@abs.com"));

        org.junit.jupiter.api.Assertions.assertEquals(2, userRepository.count());
    }

    @Test
    void concurrentRegistrationsKeepEmailAndEmployeeIdsUnique() throws Exception {
        ExecutorService executor = Executors.newFixedThreadPool(2);
        CountDownLatch ready = new CountDownLatch(2);
        CountDownLatch start = new CountDownLatch(1);
        try {
            Future<String> firstResponse = executor.submit(() -> registerConcurrently(ready, start));
            Future<String> secondResponse = executor.submit(() -> registerConcurrently(ready, start));
            ready.await();
            start.countDown();

            JsonNode first = objectMapper.readTree(firstResponse.get());
            JsonNode second = objectMapper.readTree(secondResponse.get());
            assertEquals(true, first.path("success").asBoolean());
            assertEquals(true, second.path("success").asBoolean());

            Set<String> emailIds = new HashSet<>();
            emailIds.add(first.path("data").path("emailId").asText());
            emailIds.add(second.path("data").path("emailId").asText());
            assertEquals(2, emailIds.size());

            Set<String> employeeIds = new HashSet<>();
            employeeIds.add(first.path("data").path("userId").asText());
            employeeIds.add(second.path("data").path("userId").asText());
            assertEquals(2, employeeIds.size());
            assertEquals(2, userRepository.count());
        } finally {
            executor.shutdownNow();
        }
    }

    private String registerConcurrently(CountDownLatch ready, CountDownLatch start) throws Exception {
        ready.countDown();
        start.await();
        return mockMvc.perform(post("/api/v1/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"firstName\":\"Ada\",\"lastName\":\"Lovelace\"}"))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString();
    }
}
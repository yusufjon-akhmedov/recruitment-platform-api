package com.yusufjon.recruitmentplatform.integration;

/**
 * Runs end-to-end integration tests for registration, login, invalid credentials, and request
 * validation on auth endpoints.
 */

import com.yusufjon.recruitmentplatform.integration.support.AbstractIntegrationTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MvcResult;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@DisplayName("Auth integration tests")
class AuthIT extends AbstractIntegrationTest {

    @Test
    @DisplayName("registration works")
    void registrationWorks() throws Exception {
        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(asJson(java.util.Map.of(
                                "fullName", "Recruiter One",
                                "email", "recruiter@example.com",
                                "password", "secret123",
                                "role", "RECRUITER"
                        ))))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.token").isString())
                .andExpect(jsonPath("$.token").isNotEmpty());
    }

    @Test
    @DisplayName("login works")
    void loginWorks() throws Exception {
        registerUserAndGetToken("Recruiter One", "recruiter@example.com", "secret123", "RECRUITER");

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(asJson(java.util.Map.of(
                                "email", "recruiter@example.com",
                                "password", "secret123"
                        ))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").isString())
                .andExpect(jsonPath("$.token").isNotEmpty());
    }

    @Test
    @DisplayName("invalid login fails correctly")
    void invalidLoginFailsCorrectly() throws Exception {
        registerUserAndGetToken("Recruiter One", "recruiter@example.com", "secret123", "RECRUITER");

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(asJson(java.util.Map.of(
                                "email", "recruiter@example.com",
                                "password", "wrong-pass"
                        ))))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.error").value("Bad Request"))
                .andExpect(jsonPath("$.message").value("Invalid email or password"))
                .andExpect(jsonPath("$.path").value("/api/auth/login"));
    }

    @Test
    @DisplayName("validation errors return proper status")
    void validationErrorsReturnProperStatus() throws Exception {
        MvcResult result = mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(asJson(java.util.Map.of(
                                "fullName", "",
                                "email", "candidate@example.com",
                                "password", "secret123",
                                "role", "CANDIDATE"
                        ))))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.error").value("Bad Request"))
                .andExpect(jsonPath("$.message").value("Full name is required"))
                .andExpect(jsonPath("$.path").value("/api/auth/register"))
                .andReturn();

        assertFalse(result.getResponse().getContentAsString().isBlank());
    }
}

package com.yusufjon.recruitmentplatform.integration;

/**
 * Runs end-to-end integration tests for registration, login, invalid credentials, and request
 * validation on auth endpoints.
 */

import com.yusufjon.recruitmentplatform.integration.support.AbstractIntegrationTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MvcResult;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.mockito.ArgumentMatchers.contains;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.times;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
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

        verify(emailSender).sendEmail(
                eq("recruiter@example.com"),
                eq("Verify your email"),
                contains("/api/auth/verify-email?token=")
        );
        org.junit.jupiter.api.Assertions.assertFalse(isEmailVerified("recruiter@example.com"));
        org.junit.jupiter.api.Assertions.assertEquals(1, countVerificationTokens("recruiter@example.com"));
    }

    @Test
    @DisplayName("login before verification fails")
    void loginBeforeVerificationFails() throws Exception {
        registerUser("Recruiter One", "recruiter@example.com", "secret123", "RECRUITER");

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(asJson(java.util.Map.of(
                                "email", "recruiter@example.com",
                                "password", "secret123"
                        ))))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Email is not verified"))
                .andExpect(jsonPath("$.path").value("/api/auth/login"));
    }

    @Test
    @DisplayName("verify email endpoint verifies user successfully")
    void verifyEmailEndpointVerifiesUserSuccessfully() throws Exception {
        registerUser("Recruiter One", "recruiter@example.com", "secret123", "RECRUITER");
        String verificationToken = getVerificationToken("recruiter@example.com");

        mockMvc.perform(get("/api/auth/verify-email").param("token", verificationToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Email verified successfully"));

        org.junit.jupiter.api.Assertions.assertTrue(isEmailVerified("recruiter@example.com"));
        org.junit.jupiter.api.Assertions.assertTrue(isVerificationTokenUsed(verificationToken));
    }

    @Test
    @DisplayName("login after verification succeeds")
    void loginAfterVerificationSucceeds() throws Exception {
        registerUser("Recruiter One", "recruiter@example.com", "secret123", "RECRUITER");
        verifyEmailForUser("recruiter@example.com");

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

    @Test
    @DisplayName("resend verification works for unverified user")
    void resendVerificationWorksForUnverifiedUser() throws Exception {
        registerUser("Recruiter One", "recruiter@example.com", "secret123", "RECRUITER");
        String firstToken = getVerificationToken("recruiter@example.com");
        Mockito.reset(emailSender);

        mockMvc.perform(post("/api/auth/resend-verification")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(asJson(java.util.Map.of("email", "recruiter@example.com"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Verification email sent successfully"));

        String resentToken = getVerificationToken("recruiter@example.com");
        assertNotEquals(firstToken, resentToken);
        org.junit.jupiter.api.Assertions.assertEquals(1, countVerificationTokens("recruiter@example.com"));
        verify(emailSender, times(1)).sendEmail(
                eq("recruiter@example.com"),
                eq("Verify your email"),
                contains(resentToken)
        );
    }

    @Test
    @DisplayName("resend verification fails for already verified user")
    void resendVerificationFailsForAlreadyVerifiedUser() throws Exception {
        registerUser("Recruiter One", "recruiter@example.com", "secret123", "RECRUITER");
        verifyEmailForUser("recruiter@example.com");

        mockMvc.perform(post("/api/auth/resend-verification")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(asJson(java.util.Map.of("email", "recruiter@example.com"))))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Email is already verified"))
                .andExpect(jsonPath("$.path").value("/api/auth/resend-verification"));
    }

    @Test
    @DisplayName("invalid token fails")
    void invalidTokenFails() throws Exception {
        mockMvc.perform(get("/api/auth/verify-email").param("token", "missing-token"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Invalid verification token"))
                .andExpect(jsonPath("$.path").value("/api/auth/verify-email"));
    }

    @Test
    @DisplayName("expired token fails")
    void expiredTokenFails() throws Exception {
        registerUser("Recruiter One", "recruiter@example.com", "secret123", "RECRUITER");
        String token = getVerificationToken("recruiter@example.com");
        expireVerificationToken(token);

        mockMvc.perform(get("/api/auth/verify-email").param("token", token))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Verification token has expired"))
                .andExpect(jsonPath("$.path").value("/api/auth/verify-email"));
    }

    @Test
    @DisplayName("register token cannot access protected endpoints before verification")
    void registerTokenCannotAccessProtectedEndpointsBeforeVerification() throws Exception {
        String registerToken = registerUser("Candidate One", "candidate@example.com", "secret123", "CANDIDATE");

        mockMvc.perform(get("/api/applications/my")
                        .header("Authorization", bearerToken(registerToken)))
                .andExpect(status().isForbidden());
    }
}

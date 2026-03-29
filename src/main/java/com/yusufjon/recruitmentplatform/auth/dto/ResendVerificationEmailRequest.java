package com.yusufjon.recruitmentplatform.auth.dto;

/**
 * Holds the request payload used to resend an email verification link.
 */

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public class ResendVerificationEmailRequest {

    @Email(message = "Email is invalid")
    @NotBlank(message = "Email is required")
    private String email;

    public ResendVerificationEmailRequest() {
    }

    public ResendVerificationEmailRequest(String email) {
        this.email = email;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }
}

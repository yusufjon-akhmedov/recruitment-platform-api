package com.yusufjon.recruitmentplatform.auth.dto;

/**
 * Represents the API response payload returned for auth data.
 */

public class AuthResponse {

    private String token;

    public AuthResponse() {
    }

    public AuthResponse(String token) {
        this.token = token;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }
}
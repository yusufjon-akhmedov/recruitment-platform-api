package com.yusufjon.recruitmentplatform.auth.controller;

/**
 * Exposes authentication endpoints for user registration and login requests.
 */

import com.yusufjon.recruitmentplatform.auth.dto.AuthResponse;
import com.yusufjon.recruitmentplatform.auth.dto.LoginRequest;
import com.yusufjon.recruitmentplatform.auth.dto.RegisterRequest;
import com.yusufjon.recruitmentplatform.auth.service.AuthService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/register")
    @ResponseStatus(HttpStatus.CREATED)
    public AuthResponse register(@Valid @RequestBody RegisterRequest request) {
        return authService.register(request);
    }

    @PostMapping("/login")
    public AuthResponse login(@Valid @RequestBody LoginRequest request) {
        return authService.login(request);
    }
}
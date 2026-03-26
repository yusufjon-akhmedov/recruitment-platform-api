package com.yusufjon.recruitmentplatform.auth.service;

import com.yusufjon.recruitmentplatform.auth.dto.AuthResponse;
import com.yusufjon.recruitmentplatform.auth.dto.LoginRequest;
import com.yusufjon.recruitmentplatform.auth.dto.RegisterRequest;
import com.yusufjon.recruitmentplatform.auth.security.JwtService;
import com.yusufjon.recruitmentplatform.common.exception.BadRequestException;
import com.yusufjon.recruitmentplatform.common.exception.ResourceNotFoundException;
import com.yusufjon.recruitmentplatform.user.entity.Role;
import com.yusufjon.recruitmentplatform.user.entity.User;
import com.yusufjon.recruitmentplatform.user.repository.RoleRepository;
import com.yusufjon.recruitmentplatform.user.repository.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class AuthService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    public AuthService(UserRepository userRepository,
                       RoleRepository roleRepository,
                       PasswordEncoder passwordEncoder,
                       JwtService jwtService) {
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
    }

    public AuthResponse register(RegisterRequest request) {
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new BadRequestException("Email already exists");
        }

        Role role = roleRepository.findByName(request.getRole())
                .orElseThrow(() -> new ResourceNotFoundException("Role not found"));

        User user = new User();
        user.setFullName(request.getFullName());
        user.setEmail(request.getEmail());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setRole(role);

        User savedUser = userRepository.save(user);

        String token = jwtService.generateToken(savedUser.getEmail());

        return new AuthResponse(token);
    }

    public AuthResponse login(LoginRequest request) {
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new BadRequestException("Invalid email or password"));

        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new BadRequestException("Invalid email or password");
        }

        String token = jwtService.generateToken(user.getEmail());
        return new AuthResponse(token);
    }
}
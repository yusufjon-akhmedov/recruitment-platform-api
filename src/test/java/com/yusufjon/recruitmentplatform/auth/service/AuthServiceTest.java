package com.yusufjon.recruitmentplatform.auth.service;

import com.yusufjon.recruitmentplatform.auth.dto.AuthResponse;
import com.yusufjon.recruitmentplatform.auth.dto.LoginRequest;
import com.yusufjon.recruitmentplatform.auth.dto.RegisterRequest;
import com.yusufjon.recruitmentplatform.auth.security.JwtService;
import com.yusufjon.recruitmentplatform.common.exception.BadRequestException;
import com.yusufjon.recruitmentplatform.common.exception.ResourceNotFoundException;
import com.yusufjon.recruitmentplatform.shared.enums.RoleName;
import com.yusufjon.recruitmentplatform.user.entity.Role;
import com.yusufjon.recruitmentplatform.user.entity.User;
import com.yusufjon.recruitmentplatform.user.repository.RoleRepository;
import com.yusufjon.recruitmentplatform.user.repository.UserRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("AuthService")
class AuthServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private RoleRepository roleRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtService jwtService;

    @InjectMocks
    private AuthService authService;

    @Test
    @DisplayName("should register a user successfully")
    void shouldRegisterUserSuccessfully() {
        RegisterRequest request = new RegisterRequest(
                "John Recruiter",
                "john@example.com",
                "secret123",
                RoleName.RECRUITER
        );
        Role recruiterRole = role(RoleName.RECRUITER);
        User savedUser = new User(1L, request.getFullName(), request.getEmail(), "encoded-secret", recruiterRole, LocalDateTime.now());

        when(userRepository.existsByEmail(request.getEmail())).thenReturn(false);
        when(roleRepository.findByName(RoleName.RECRUITER)).thenReturn(Optional.of(recruiterRole));
        when(passwordEncoder.encode(request.getPassword())).thenReturn("encoded-secret");
        when(userRepository.save(any(User.class))).thenReturn(savedUser);
        when(jwtService.generateToken(request.getEmail())).thenReturn("jwt-token");

        AuthResponse response = authService.register(request);

        ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(userCaptor.capture());

        User userToSave = userCaptor.getValue();
        assertEquals(request.getFullName(), userToSave.getFullName());
        assertEquals(request.getEmail(), userToSave.getEmail());
        assertEquals("encoded-secret", userToSave.getPassword());
        assertEquals(recruiterRole, userToSave.getRole());
        assertEquals("jwt-token", response.getToken());
    }

    @Test
    @DisplayName("should fail registration when email already exists")
    void shouldFailRegistrationWhenEmailAlreadyExists() {
        RegisterRequest request = new RegisterRequest(
                "John Recruiter",
                "john@example.com",
                "secret123",
                RoleName.RECRUITER
        );

        when(userRepository.existsByEmail(request.getEmail())).thenReturn(true);

        BadRequestException exception = assertThrows(BadRequestException.class, () -> authService.register(request));

        assertEquals("Email already exists", exception.getMessage());
        verify(roleRepository, never()).findByName(any());
        verify(userRepository, never()).save(any(User.class));
        verifyNoInteractions(passwordEncoder, jwtService);
    }

    @Test
    @DisplayName("should fail registration when role is not found")
    void shouldFailRegistrationWhenRoleIsNotFound() {
        RegisterRequest request = new RegisterRequest(
                "John Recruiter",
                "john@example.com",
                "secret123",
                RoleName.RECRUITER
        );

        when(userRepository.existsByEmail(request.getEmail())).thenReturn(false);
        when(roleRepository.findByName(RoleName.RECRUITER)).thenReturn(Optional.empty());

        ResourceNotFoundException exception = assertThrows(
                ResourceNotFoundException.class,
                () -> authService.register(request)
        );

        assertEquals("Role not found", exception.getMessage());
        verify(userRepository, never()).save(any(User.class));
        verifyNoInteractions(passwordEncoder, jwtService);
    }

    @Test
    @DisplayName("should login successfully")
    void shouldLoginSuccessfully() {
        LoginRequest request = new LoginRequest("john@example.com", "secret123");
        User existingUser = new User(
                1L,
                "John Recruiter",
                request.getEmail(),
                "encoded-secret",
                role(RoleName.RECRUITER),
                LocalDateTime.now()
        );

        when(userRepository.findByEmail(request.getEmail())).thenReturn(Optional.of(existingUser));
        when(passwordEncoder.matches(request.getPassword(), existingUser.getPassword())).thenReturn(true);
        when(jwtService.generateToken(request.getEmail())).thenReturn("jwt-token");

        AuthResponse response = authService.login(request);

        assertEquals("jwt-token", response.getToken());
        verify(jwtService).generateToken(request.getEmail());
    }

    @Test
    @DisplayName("should fail login when email does not exist")
    void shouldFailLoginWhenEmailDoesNotExist() {
        LoginRequest request = new LoginRequest("missing@example.com", "secret123");

        when(userRepository.findByEmail(request.getEmail())).thenReturn(Optional.empty());

        BadRequestException exception = assertThrows(BadRequestException.class, () -> authService.login(request));

        assertEquals("Invalid email or password", exception.getMessage());
        verifyNoInteractions(passwordEncoder, jwtService);
    }

    @Test
    @DisplayName("should fail login when password does not match")
    void shouldFailLoginWhenPasswordDoesNotMatch() {
        LoginRequest request = new LoginRequest("john@example.com", "wrong-password");
        User existingUser = new User(
                1L,
                "John Recruiter",
                request.getEmail(),
                "encoded-secret",
                role(RoleName.RECRUITER),
                LocalDateTime.now()
        );

        when(userRepository.findByEmail(request.getEmail())).thenReturn(Optional.of(existingUser));
        when(passwordEncoder.matches(request.getPassword(), existingUser.getPassword())).thenReturn(false);

        BadRequestException exception = assertThrows(BadRequestException.class, () -> authService.login(request));

        assertEquals("Invalid email or password", exception.getMessage());
        verify(jwtService, never()).generateToken(any());
        assertTrue(exception.getMessage().contains("Invalid email or password"));
    }

    private Role role(RoleName roleName) {
        return new Role(1L, roleName);
    }
}

package com.yusufjon.recruitmentplatform.auth.service;

/**
 * Contains unit tests that verify email verification token creation, validation, and resend
 * behavior by mocking persistence and mail collaborators.
 */

import com.yusufjon.recruitmentplatform.auth.entity.EmailVerificationToken;
import com.yusufjon.recruitmentplatform.auth.repository.EmailVerificationTokenRepository;
import com.yusufjon.recruitmentplatform.common.exception.BadRequestException;
import com.yusufjon.recruitmentplatform.common.exception.ResourceNotFoundException;
import com.yusufjon.recruitmentplatform.shared.enums.RoleName;
import com.yusufjon.recruitmentplatform.user.entity.Role;
import com.yusufjon.recruitmentplatform.user.entity.User;
import com.yusufjon.recruitmentplatform.user.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.contains;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("EmailVerificationService")
class EmailVerificationServiceTest {

    @Mock
    private EmailVerificationTokenRepository emailVerificationTokenRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private EmailSender emailSender;

    private EmailVerificationService emailVerificationService;

    @BeforeEach
    void setUp() {
        emailVerificationService = new EmailVerificationService(
                emailVerificationTokenRepository,
                userRepository,
                emailSender,
                "http://localhost:8080/api/auth/verify-email",
                24
        );
    }

    @Test
    @DisplayName("should create and send a verification token")
    void shouldCreateAndSendVerificationToken() {
        User user = unverifiedUser();

        when(emailVerificationTokenRepository.findByUser(user)).thenReturn(Optional.empty());
        when(emailVerificationTokenRepository.save(any(EmailVerificationToken.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        emailVerificationService.createVerificationTokenAndSendEmail(user);

        ArgumentCaptor<EmailVerificationToken> tokenCaptor = ArgumentCaptor.forClass(EmailVerificationToken.class);
        verify(emailVerificationTokenRepository).save(tokenCaptor.capture());

        EmailVerificationToken savedToken = tokenCaptor.getValue();
        assertEquals(user, savedToken.getUser());
        assertNotNull(savedToken.getToken());
        assertTrue(savedToken.getExpiresAt().isAfter(LocalDateTime.now()));
        verify(emailSender).sendEmail(
                eq(user.getEmail()),
                eq("Verify your email"),
                contains(savedToken.getToken())
        );
    }

    @Test
    @DisplayName("should verify email successfully")
    void shouldVerifyEmailSuccessfully() {
        User user = unverifiedUser();
        EmailVerificationToken token = validToken(user);

        when(emailVerificationTokenRepository.findByToken("valid-token")).thenReturn(Optional.of(token));

        emailVerificationService.verifyEmail("valid-token");

        assertTrue(user.isEmailVerified());
        assertNotNull(token.getVerifiedAt());
        verify(userRepository).save(user);
        verify(emailVerificationTokenRepository).save(token);
    }

    @Test
    @DisplayName("should fail verification when token is invalid")
    void shouldFailVerificationWhenTokenIsInvalid() {
        when(emailVerificationTokenRepository.findByToken("missing-token")).thenReturn(Optional.empty());

        BadRequestException exception = assertThrows(
                BadRequestException.class,
                () -> emailVerificationService.verifyEmail("missing-token")
        );

        assertEquals("Invalid verification token", exception.getMessage());
    }

    @Test
    @DisplayName("should fail verification when token is expired")
    void shouldFailVerificationWhenTokenIsExpired() {
        User user = unverifiedUser();
        EmailVerificationToken token = validToken(user);
        token.setExpiresAt(LocalDateTime.now().minusHours(1));

        when(emailVerificationTokenRepository.findByToken("valid-token")).thenReturn(Optional.of(token));

        BadRequestException exception = assertThrows(
                BadRequestException.class,
                () -> emailVerificationService.verifyEmail("valid-token")
        );

        assertEquals("Verification token has expired", exception.getMessage());
    }

    @Test
    @DisplayName("should fail verification when token is already used")
    void shouldFailVerificationWhenTokenIsAlreadyUsed() {
        User user = unverifiedUser();
        EmailVerificationToken token = validToken(user);
        token.setVerifiedAt(LocalDateTime.now());

        when(emailVerificationTokenRepository.findByToken("valid-token")).thenReturn(Optional.of(token));

        BadRequestException exception = assertThrows(
                BadRequestException.class,
                () -> emailVerificationService.verifyEmail("valid-token")
        );

        assertEquals("Verification token has already been used", exception.getMessage());
    }

    @Test
    @DisplayName("should resend verification email for unverified user")
    void shouldResendVerificationEmailForUnverifiedUser() {
        User user = unverifiedUser();
        EmailVerificationToken existingToken = validToken(user);

        when(userRepository.findByEmail(user.getEmail())).thenReturn(Optional.of(user));
        when(emailVerificationTokenRepository.findByUser(user)).thenReturn(Optional.of(existingToken));
        when(emailVerificationTokenRepository.save(any(EmailVerificationToken.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        emailVerificationService.resendVerificationEmail(user.getEmail());

        verify(emailVerificationTokenRepository).delete(existingToken);
        verify(emailVerificationTokenRepository).flush();
        verify(emailSender).sendEmail(
                eq(user.getEmail()),
                eq("Verify your email"),
                contains("http://localhost:8080/api/auth/verify-email?token=")
        );
    }

    @Test
    @DisplayName("should fail resend when user is already verified")
    void shouldFailResendWhenUserIsAlreadyVerified() {
        User user = verifiedUser();

        when(userRepository.findByEmail(user.getEmail())).thenReturn(Optional.of(user));

        BadRequestException exception = assertThrows(
                BadRequestException.class,
                () -> emailVerificationService.resendVerificationEmail(user.getEmail())
        );

        assertEquals("Email is already verified", exception.getMessage());
        verify(emailVerificationTokenRepository, never()).save(any());
    }

    @Test
    @DisplayName("should fail resend when user does not exist")
    void shouldFailResendWhenUserDoesNotExist() {
        when(userRepository.findByEmail("missing@example.com")).thenReturn(Optional.empty());

        ResourceNotFoundException exception = assertThrows(
                ResourceNotFoundException.class,
                () -> emailVerificationService.resendVerificationEmail("missing@example.com")
        );

        assertEquals("User not found", exception.getMessage());
    }

    private User unverifiedUser() {
        User user = new User();
        user.setId(1L);
        user.setFullName("Recruiter One");
        user.setEmail("recruiter@example.com");
        user.setPassword("encoded-secret");
        user.setRole(new Role(1L, RoleName.RECRUITER));
        user.setEmailVerified(false);
        user.setCreatedAt(LocalDateTime.now());
        return user;
    }

    private User verifiedUser() {
        User user = unverifiedUser();
        user.setEmailVerified(true);
        return user;
    }

    private EmailVerificationToken validToken(User user) {
        EmailVerificationToken token = new EmailVerificationToken();
        token.setId(1L);
        token.setUser(user);
        token.setToken("valid-token");
        token.setExpiresAt(LocalDateTime.now().plusHours(1));
        token.setCreatedAt(LocalDateTime.now());
        return token;
    }
}

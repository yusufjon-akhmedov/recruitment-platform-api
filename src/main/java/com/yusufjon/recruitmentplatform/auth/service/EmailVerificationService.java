package com.yusufjon.recruitmentplatform.auth.service;

/**
 * Handles creating, sending, resending, and verifying email verification tokens.
 */

import com.yusufjon.recruitmentplatform.auth.entity.EmailVerificationToken;
import com.yusufjon.recruitmentplatform.auth.repository.EmailVerificationTokenRepository;
import com.yusufjon.recruitmentplatform.common.exception.BadRequestException;
import com.yusufjon.recruitmentplatform.common.exception.ResourceNotFoundException;
import com.yusufjon.recruitmentplatform.user.entity.User;
import com.yusufjon.recruitmentplatform.user.repository.UserRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.util.UriComponentsBuilder;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
public class EmailVerificationService {

    private final EmailVerificationTokenRepository emailVerificationTokenRepository;
    private final UserRepository userRepository;
    private final EmailSender emailSender;
    private final String verificationBaseUrl;
    private final long expirationHours;

    public EmailVerificationService(EmailVerificationTokenRepository emailVerificationTokenRepository,
                                    UserRepository userRepository,
                                    EmailSender emailSender,
                                    @Value("${app.email-verification.base-url}") String verificationBaseUrl,
                                    @Value("${app.email-verification.expiration-hours}") long expirationHours) {
        this.emailVerificationTokenRepository = emailVerificationTokenRepository;
        this.userRepository = userRepository;
        this.emailSender = emailSender;
        this.verificationBaseUrl = verificationBaseUrl;
        this.expirationHours = expirationHours;
    }

    @Transactional
    public void createVerificationTokenAndSendEmail(User user) {
        emailVerificationTokenRepository.findByUser(user).ifPresent(existingToken -> {
            emailVerificationTokenRepository.delete(existingToken);
            emailVerificationTokenRepository.flush();
        });

        EmailVerificationToken verificationToken = new EmailVerificationToken();
        verificationToken.setUser(user);
        verificationToken.setToken(UUID.randomUUID().toString());
        verificationToken.setExpiresAt(LocalDateTime.now().plusHours(expirationHours));

        EmailVerificationToken savedToken = emailVerificationTokenRepository.save(verificationToken);
        emailSender.sendEmail(
                user.getEmail(),
                "Verify your email",
                buildEmailBody(user, savedToken.getToken())
        );
    }

    @Transactional
    public void verifyEmail(String token) {
        if (token == null || token.isBlank()) {
            throw new BadRequestException("Token is required");
        }

        EmailVerificationToken verificationToken = emailVerificationTokenRepository.findByToken(token)
                .orElseThrow(() -> new BadRequestException("Invalid verification token"));

        if (verificationToken.getVerifiedAt() != null) {
            throw new BadRequestException("Verification token has already been used");
        }

        if (verificationToken.getExpiresAt().isBefore(LocalDateTime.now())) {
            throw new BadRequestException("Verification token has expired");
        }

        User user = verificationToken.getUser();

        if (user.isEmailVerified()) {
            throw new BadRequestException("Email is already verified");
        }

        user.setEmailVerified(true);
        verificationToken.setVerifiedAt(LocalDateTime.now());

        userRepository.save(user);
        emailVerificationTokenRepository.save(verificationToken);
    }

    @Transactional
    public void resendVerificationEmail(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        if (user.isEmailVerified()) {
            throw new BadRequestException("Email is already verified");
        }

        createVerificationTokenAndSendEmail(user);
    }

    private String buildEmailBody(User user, String token) {
        return "Hello " + user.getFullName() + ",\n\n"
                + "Please verify your email by visiting the link below:\n"
                + buildVerificationUrl(token) + "\n\n"
                + "This link expires in " + expirationHours + " hours.";
    }

    private String buildVerificationUrl(String token) {
        return UriComponentsBuilder.fromUriString(verificationBaseUrl)
                .queryParam("token", token)
                .build()
                .toUriString();
    }
}

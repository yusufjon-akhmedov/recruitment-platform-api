package com.yusufjon.recruitmentplatform.auth.service;

/**
 * Sends plain-text emails using Spring's mail support.
 */

import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
public class SmtpEmailSender implements EmailSender {

    private final JavaMailSender javaMailSender;
    private final String fromAddress;

    public SmtpEmailSender(JavaMailSender javaMailSender,
                           @Value("${app.mail.from:${spring.mail.username:no-reply@recruitment-platform.local}}")
                           String fromAddress) {
        this.javaMailSender = javaMailSender;
        this.fromAddress = fromAddress;
    }

    @Override
    public void sendEmail(String to, String subject, String body) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(to);
        message.setSubject(subject);
        message.setText(body);

        if (fromAddress != null && !fromAddress.isBlank()) {
            message.setFrom(fromAddress);
        }

        javaMailSender.send(message);
    }
}

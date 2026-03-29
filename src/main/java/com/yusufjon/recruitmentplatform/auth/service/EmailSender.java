package com.yusufjon.recruitmentplatform.auth.service;

/**
 * Abstraction for sending application emails so tests can stub or mock delivery.
 */

public interface EmailSender {
    void sendEmail(String to, String subject, String body);
}

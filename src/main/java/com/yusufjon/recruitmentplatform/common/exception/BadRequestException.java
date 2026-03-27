package com.yusufjon.recruitmentplatform.common.exception;

/**
 * Represents business-rule or validation failures that should be returned as HTTP 400 responses.
 */

public class BadRequestException extends RuntimeException {

    public BadRequestException(String message) {
        super(message);
    }
}
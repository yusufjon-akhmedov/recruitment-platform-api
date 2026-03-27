package com.yusufjon.recruitmentplatform.common.exception;

/**
 * Represents authorization failures when a user tries to perform an action they are not allowed to
 * do.
 */

public class ForbiddenException extends RuntimeException {

    public ForbiddenException(String message) {
        super(message);
    }
}
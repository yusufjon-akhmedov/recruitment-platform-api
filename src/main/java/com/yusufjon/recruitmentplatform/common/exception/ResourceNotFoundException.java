package com.yusufjon.recruitmentplatform.common.exception;

/**
 * Represents cases where the requested domain object does not exist.
 */

public class ResourceNotFoundException extends RuntimeException {

    public ResourceNotFoundException(String message) {
        super(message);
    }
}
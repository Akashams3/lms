package com.project.lms.exceptions;

public class DuplicateResourceException extends ApiException {

    public DuplicateResourceException(String messageKey, Object... args) {
        super(messageKey, args);
    }
}
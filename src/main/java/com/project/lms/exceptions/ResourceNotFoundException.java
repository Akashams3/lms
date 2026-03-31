package com.project.lms.exceptions;

public class ResourceNotFoundException extends ApiException {

    public ResourceNotFoundException(String messageKey, Object... args) {
        super(messageKey, args);
    }
}
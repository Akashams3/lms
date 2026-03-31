package com.project.lms.exceptions;

public class UnauthorizedActionException extends ApiException {

    public UnauthorizedActionException(String messageKey, Object... args) {
        super(messageKey, args);
    }
}
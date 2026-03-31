package com.project.lms.exceptions;

public abstract class ApiException extends RuntimeException {

  private final Object[] args;

  public ApiException(String messageKey, Object... args) {
    super(messageKey);
    this.args = args;
  }

  public Object[] getArgs() {
    return args;
  }
}
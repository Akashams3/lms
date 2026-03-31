package com.project.lms.exceptions;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    private final MessageSource messageSource;

    public GlobalExceptionHandler(MessageSource messageSource) {
        this.messageSource = messageSource;
    }

    @ExceptionHandler(ApiException.class)
    public ResponseEntity<Map<String, Object>> handleApiException(ApiException ex) {

        HttpStatus status = switch (ex) {
            case ResourceNotFoundException ignored -> HttpStatus.NOT_FOUND;
            case DuplicateResourceException ignored -> HttpStatus.CONFLICT;
            case UnauthorizedActionException ignored -> HttpStatus.FORBIDDEN;
            default -> HttpStatus.BAD_REQUEST;
        };

        return buildResponse(ex.getMessage(), ex.getArgs(), status);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, Object>> handleValidation(MethodArgumentNotValidException ex) {

        Map<String, String> errors = new HashMap<>();

        ex.getBindingResult().getFieldErrors()
                .forEach(error ->
                        errors.put(error.getField(), error.getDefaultMessage())
                );

        Map<String, Object> body = new HashMap<>();
        body.put("timestamp", LocalDateTime.now());
        body.put("status", HttpStatus.BAD_REQUEST.value());
        body.put("message", resolveMessage("VALIDATION_FAILED", null));
        body.put("errors", errors);

        return new ResponseEntity<>(body, HttpStatus.BAD_REQUEST);
    }


    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<Map<String, Object>> handleDB(DataIntegrityViolationException ex) {

        log.error("Database Error", ex);

        return buildResponse("INTERNAL_ERROR", null, HttpStatus.CONFLICT);
    }


    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, Object>> handleAll(Exception ex) {

        log.error("Unhandled Exception", ex);

        return buildResponse("INTERNAL_ERROR", null, HttpStatus.INTERNAL_SERVER_ERROR);
    }
    @ExceptionHandler(com.twilio.exception.ApiException.class)
    public ResponseEntity<Map<String, Object>> handleTwilioException(com.twilio.exception.ApiException ex) {

        log.error("Twilio API error: code={}, message={}", ex.getCode(), ex.getMessage());

        String messageKey = "INVALID_TWILIO_NUMBER";

        return buildResponse(messageKey, null, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(BadCredentialsException.class)
    public ResponseEntity<?> handleBadCredentialsException(BadCredentialsException ex) {

        log.error("Login failed due to invalid credentials");

        return ResponseEntity
                .status(HttpStatus.UNAUTHORIZED)
                .body("Invalid email or password");
    }

    private ResponseEntity<Map<String, Object>> buildResponse(
            String messageKey,
            Object[] args,
            HttpStatus status
    ) {

        Map<String, Object> body = new HashMap<>();
        body.put("timestamp", LocalDateTime.now());
        body.put("status", status.value());
        body.put("message", resolveMessage(messageKey, args));

        return new ResponseEntity<>(body, status);
    }

    private String resolveMessage(String key, Object[] args) {
        return messageSource.getMessage(
                key,
                args,
                key,
                LocaleContextHolder.getLocale()
        );
    }
}
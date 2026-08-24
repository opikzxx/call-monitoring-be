package com.callmonitoring.backend.exception;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import java.util.LinkedHashMap;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {
    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(AuthenticationException.class)
    public ResponseEntity<Map<String, Object>> handleAuthenticationException(AuthenticationException ex) {
        var error = ErrorResponse.of(HttpStatus.UNAUTHORIZED, "Invalid email or password");
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(error.toMap());
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<Map<String, Object>> handleUnreadableBody(HttpMessageNotReadableException ex) {
        var error = ErrorResponse.of(HttpStatus.BAD_REQUEST, "Request body is missing or malformed");
        return ResponseEntity.badRequest().body(error.toMap());
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Map<String, Object>> handleIllegalArgument(IllegalArgumentException ex) {
        var error = ErrorResponse.of(HttpStatus.BAD_REQUEST, ex.getMessage());
        return ResponseEntity.badRequest().body(error.toMap());
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<Map<String, Object>> handleTypeMismatch(MethodArgumentTypeMismatchException ex) {
        var error = ErrorResponse.of(HttpStatus.BAD_REQUEST, "Invalid value for parameter '" + ex.getName() + "'");
        return ResponseEntity.badRequest().body(error.toMap());
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, Object>> handleUnexpected(Exception ex) {
        log.error("Unhandled exception", ex);
        var error = ErrorResponse.of(HttpStatus.INTERNAL_SERVER_ERROR, "An unexpected error occurred");
        return ResponseEntity.internalServerError().body(error.toMap());
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, Object>> handleValidationException(MethodArgumentNotValidException ex) {
        Map<String, String> fieldErrors = new LinkedHashMap<>();
        ex.getBindingResult().getFieldErrors().forEach(fieldError ->
            fieldErrors.put(fieldError.getField(), fieldError.getDefaultMessage())
        );

        var error = ErrorResponse.of(HttpStatus.BAD_REQUEST, "Validation failed", fieldErrors);
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error.toMap());
    }
}

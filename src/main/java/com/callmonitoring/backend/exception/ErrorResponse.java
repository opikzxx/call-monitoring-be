package com.callmonitoring.backend.exception;

import org.springframework.http.HttpStatus;

import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;

public record ErrorResponse(String timestamp, int status, String error, String message, Map<String, String> fieldErrors) {

    public static ErrorResponse of(HttpStatus status, String message) {
        return new ErrorResponse(Instant.now().toString(), status.value(), status.getReasonPhrase(), message, null);
    }

    public static ErrorResponse of(HttpStatus status, String message, Map<String, String> fieldErrors) {
        return new ErrorResponse(Instant.now().toString(), status.value(), status.getReasonPhrase(), message, fieldErrors);
    }

    public Map<String, Object> toMap() {
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("timestamp", timestamp);
        map.put("status", status);
        map.put("error", error);
        map.put("message", message);
        if (fieldErrors != null) {
            map.put("fieldErrors", fieldErrors);
        }
        return map;
    }

    public String toJson() {
        return "{\"timestamp\":\"%s\",\"status\":%d,\"error\":\"%s\",\"message\":\"%s\"}"
            .formatted(timestamp, status, error, message);
    }
}

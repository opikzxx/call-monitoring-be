package com.callmonitoring.backend.dto.response;

public record TokenResponse(String accessToken, String tokenType, long expiresInMs) {
}

package com.callmonitoring.backend.security;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class JwtTokenProviderTest {
    private final JwtTokenProvider jwtTokenProvider = new JwtTokenProvider(
        "test-secret-key-must-be-at-least-32-bytes-long-1234567890",
        3600000L
    );

    @Test
    void generatesTokenThatIsValidAndCarriesTheSubjectEmail() {
        String token = jwtTokenProvider.generateAccessToken("supervisor@callmonitoring.test");

        assertThat(jwtTokenProvider.isTokenValid(token)).isTrue();
        assertThat(jwtTokenProvider.getEmailFromToken(token)).isEqualTo("supervisor@callmonitoring.test");
    }

    @Test
    void rejectsMalformedToken() {
        assertThat(jwtTokenProvider.isTokenValid("not-a-real-jwt")).isFalse();
    }

    @Test
    void rejectsTokenSignedWithADifferentSecret() {
        JwtTokenProvider otherProvider = new JwtTokenProvider(
            "a-completely-different-secret-key-1234567890123456",
            3600000L
        );
        String token = otherProvider.generateAccessToken("supervisor@callmonitoring.test");

        assertThat(jwtTokenProvider.isTokenValid(token)).isFalse();
    }

    @Test
    void rejectsExpiredToken() {
        JwtTokenProvider expiringProvider = new JwtTokenProvider(
            "test-secret-key-must-be-at-least-32-bytes-long-1234567890",
            -1000L
        );
        String token = expiringProvider.generateAccessToken("supervisor@callmonitoring.test");

        assertThat(jwtTokenProvider.isTokenValid(token)).isFalse();
    }
}

package com.callmonitoring.backend.service;

import com.callmonitoring.backend.dto.request.LoginRequest;
import com.callmonitoring.backend.dto.response.TokenResponse;
import com.callmonitoring.backend.security.JwtTokenProvider;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {
    @Mock
    private AuthenticationManager authenticationManager;

    @Mock
    private JwtTokenProvider jwtTokenProvider;

    private AuthService authService;

    @Test
    void loginAuthenticatesCredentialsAndReturnsAnAccessToken() {
        authService = new AuthService(authenticationManager, jwtTokenProvider);
        LoginRequest request = new LoginRequest();
        request.setEmail("supervisor@callmonitoring.test");
        request.setPassword("password123");

        when(jwtTokenProvider.generateAccessToken("supervisor@callmonitoring.test")).thenReturn("signed.jwt.token");
        when(jwtTokenProvider.getExpirationMs()).thenReturn(3600000L);

        TokenResponse response = authService.login(request);

        assertThat(response.accessToken()).isEqualTo("signed.jwt.token");
        assertThat(response.tokenType()).isEqualTo("Bearer");
        assertThat(response.expiresInMs()).isEqualTo(3600000L);

        ArgumentCaptor<UsernamePasswordAuthenticationToken> captor =
            ArgumentCaptor.forClass(UsernamePasswordAuthenticationToken.class);
        verify(authenticationManager).authenticate(captor.capture());
        assertThat(captor.getValue().getPrincipal()).isEqualTo("supervisor@callmonitoring.test");
        assertThat(captor.getValue().getCredentials()).isEqualTo("password123");
    }

    @Test
    void loginPropagatesAuthenticationFailureWithoutIssuingAToken() {
        authService = new AuthService(authenticationManager, jwtTokenProvider);
        LoginRequest request = new LoginRequest();
        request.setEmail("supervisor@callmonitoring.test");
        request.setPassword("wrong-password");

        when(authenticationManager.authenticate(any())).thenThrow(new BadCredentialsException("bad credentials"));

        assertThatThrownBy(() -> authService.login(request)).isInstanceOf(BadCredentialsException.class);
    }
}

package com.callmonitoring.backend.service;

import com.callmonitoring.backend.dto.request.LoginRequest;
import com.callmonitoring.backend.dto.response.TokenResponse;
import com.callmonitoring.backend.security.JwtTokenProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthService {
    private final AuthenticationManager authenticationManager;
    private final JwtTokenProvider jwtTokenProvider;

    public TokenResponse login(LoginRequest request) {
        authenticationManager.authenticate(
            new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword())
        );

        String accessToken = jwtTokenProvider.generateAccessToken(request.getEmail());
        return new TokenResponse(accessToken, "Bearer", jwtTokenProvider.getExpirationMs());
    }
}

package com.siddharthgawas.apigateway.service.impl;

import com.siddharthgawas.apigateway.configuration.ApplicationProperties;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collections;

import static org.assertj.core.api.Assertions.*;

class JWTServiceImplTest {
    private JWTServiceImpl jwtService;

    @BeforeEach
    void setUp() {
        ApplicationProperties applicationProperties = Mockito.mock(ApplicationProperties.class);
        String secret = "test-secret-key-1234567890";
        Mockito.when(applicationProperties.getSecret()).thenReturn(secret);
        // 15 minutes
        long accessTokenExpiration = 1000 * 60 * 15;
        Mockito.when(applicationProperties.getJwtAccessTokenExpirationMs()).thenReturn(accessTokenExpiration);
        // 7 days
        long refreshTokenExpiration = 1000 * 60 * 60 * 24 * 7;
        Mockito.when(applicationProperties.getJwtRefreshTokenExpirationMs()).thenReturn(refreshTokenExpiration);
        jwtService = new JWTServiceImpl(applicationProperties);
    }

    @Test
    void generateAccessToken_and_validateToken() {
        UserDetails user = new User("tester", "password", Collections.emptyList());
        String token = jwtService.generateAccessToken(user);
        assertThat(token).isNotBlank();
        String username = jwtService.getUserFromToken(token).getUsername();
        assertThat(username).isEqualTo("tester");
        assertThat(jwtService.validateToken(token)).isTrue();
    }

    @Test
    void generateRefreshToken_and_validateToken() {
        UserDetails user = new User("refresher", "password", Collections.emptyList());
        String token = jwtService.generateRefreshToken(user);
        assertThat(token).isNotBlank();
        assertThat(jwtService.validateToken(token)).isTrue();
    }

    @Test
    void isTokenValid_shouldReturnFalseForInvalidToken() {
        String invalidToken = "invalid.token.value";
        assertThat(jwtService.validateToken(invalidToken)).isFalse();
    }

    @Test
    void validateToken_shouldReturnFalseForMalformedToken() {
        String malformedToken = "not.a.jwt.token";
        boolean isValid = jwtService.validateToken(malformedToken);
        assertThat(isValid).isFalse();
    }

    @Test
    void getUserFromToken_shouldReturnNullForMalformedToken() {
        String malformedToken = "not.a.jwt.token";
        User user = jwtService.getUserFromToken(malformedToken);
        assertThat(user).isNull();
    }
}

package com.siddharthgawas.apigateway.service.impl;

import com.siddharthgawas.apigateway.security.dto.TokenDetails;
import com.siddharthgawas.apigateway.service.JWTService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Collections;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

class AuthenticationServiceImplTest {
    private JWTService jwtService;
    private AuthenticationServiceImpl authenticationService;

    @BeforeEach
    void setUp() {
        PasswordEncoder passwordEncoder = mock(PasswordEncoder.class);
        jwtService = mock(JWTService.class);
        when(passwordEncoder.encode(anyString())).thenAnswer(invocation -> invocation.getArgument(0));
        when(passwordEncoder.matches(anyString(), anyString())).thenAnswer(invocation -> {
            String raw = invocation.getArgument(0);
            String encoded = invocation.getArgument(1);
            return raw.equals(encoded);
        });
        authenticationService = new AuthenticationServiceImpl(passwordEncoder, jwtService);
    }

    @Test
    void authenticate_shouldReturnTokenDetails_whenCredentialsAreValid() {
        String username = "johndoe";
        String password = "johndoe";
        User user = new User(username, password, Collections.emptyList());
        String accessToken = "access-token";
        String refreshToken = "refresh-token";
        when(jwtService.generateAccessToken(any(User.class))).thenReturn(accessToken);
        when(jwtService.generateRefreshToken(any(User.class))).thenReturn(refreshToken);

        TokenDetails result = authenticationService.authenticate(username, password);

        assertThat(result.accessToken()).isEqualTo(accessToken);
        assertThat(result.refreshToken()).isEqualTo(refreshToken);
    }

    @Test
    void authenticate_shouldThrowAccessDeniedException_whenUserNotFound() {
        assertThatThrownBy(() -> authenticationService.authenticate("nouser", "pass"))
                .isInstanceOf(AccessDeniedException.class)
                .hasMessageContaining("Invalid username or password");
    }

    @Test
    void authenticate_shouldThrowAccessDeniedException_whenPasswordInvalid() {
        assertThatThrownBy(() -> authenticationService.authenticate("johndoe", "wrongpass"))
                .isInstanceOf(AccessDeniedException.class)
                .hasMessageContaining("Invalid username or password");
    }

    @Test
    void refreshAccessToken_shouldReturnTokenDetails_whenRefreshTokenValid() {
        String refreshToken = "refresh-token";
        User user = new User("johndoe", "johndoe", Collections.emptyList());
        String accessToken = "access-token";
        String newRefreshToken = "new-refresh-token";
        when(jwtService.validateToken(refreshToken)).thenReturn(true);
        when(jwtService.getUserFromToken(refreshToken)).thenReturn(user);
        when(jwtService.generateAccessToken(user)).thenReturn(accessToken);
        when(jwtService.generateRefreshToken(user)).thenReturn(newRefreshToken);

        TokenDetails result = authenticationService.refreshAccessToken(refreshToken);

        assertThat(result.accessToken()).isEqualTo(accessToken);
        assertThat(result.refreshToken()).isEqualTo(newRefreshToken);
    }

    @Test
    void refreshAccessToken_shouldThrowAccessDeniedException_whenTokenInvalid() {
        String refreshToken = "invalid-token";
        when(jwtService.validateToken(refreshToken)).thenReturn(false);
        assertThatThrownBy(() -> authenticationService.refreshAccessToken(refreshToken))
                .isInstanceOf(AccessDeniedException.class)
                .hasMessageContaining("Invalid refresh token");
    }

    @Test
    void refreshAccessToken_shouldThrowAccessDeniedException_whenUserNull() {
        String refreshToken = "refresh-token";
        when(jwtService.validateToken(refreshToken)).thenReturn(true);
        when(jwtService.getUserFromToken(refreshToken)).thenReturn(null);
        assertThatThrownBy(() -> authenticationService.refreshAccessToken(refreshToken))
                .isInstanceOf(AccessDeniedException.class)
                .hasMessageContaining("No user found for the provided refresh token");
    }
}


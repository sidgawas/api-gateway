package com.siddharthgawas.apigateway.security;

import com.siddharthgawas.apigateway.security.dto.JWTAuthentication;
import com.siddharthgawas.apigateway.service.JWTService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.AuthenticationServiceException;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

class JWTAuthenticationProviderTest {
    private JWTService jwtService;
    private JWTAuthenticationProvider provider;

    @BeforeEach
    void setUp() {
        jwtService = mock(JWTService.class);
        provider = new JWTAuthenticationProvider(jwtService);
    }

    @Test
    void authenticate_shouldReturnAuthenticatedJWTAuthentication_whenTokenValidAndUserFound() {
        String token = "valid-token";
        User userDetails = new User("user", "pass", java.util.Collections.emptyList());
        JWTAuthentication authentication = new JWTAuthentication(token);
        when(jwtService.validateToken(token)).thenReturn(true);
        when(jwtService.getUserFromToken(token)).thenReturn(userDetails);

        JWTAuthentication result = (JWTAuthentication) provider.authenticate(authentication);
        assertThat(result.isAuthenticated()).isTrue();
        assertThat(result.getPrincipal()).isNotNull();
        assertThat(result.getName()).isEqualTo("user");
    }

    @Test
    void authenticate_shouldThrowException_whenTokenInvalid() {
        String token = "invalid-token";
        JWTAuthentication authentication = new JWTAuthentication(token);
        when(jwtService.validateToken(token)).thenReturn(false);

        assertThatThrownBy(() -> provider.authenticate(authentication))
                .isInstanceOf(AuthenticationServiceException.class)
                .hasMessageContaining("Invalid access token");
    }

    @Test
    void authenticate_shouldThrowException_whenUserNotFound() {
        String token = "valid-token";
        JWTAuthentication authentication = new JWTAuthentication(token);
        when(jwtService.validateToken(token)).thenReturn(true);
        when(jwtService.getUserFromToken(token)).thenReturn(null);

        assertThatThrownBy(() -> provider.authenticate(authentication))
                .isInstanceOf(AuthenticationServiceException.class)
                .hasMessageContaining("No user found for the provided access token");
    }

    @Test
    void authenticate_shouldReturnInput_whenNotJWTAuthentication() {
        AuthenticationProvider provider = new JWTAuthenticationProvider(jwtService);
        org.springframework.security.authentication.UsernamePasswordAuthenticationToken otherAuth =
                new org.springframework.security.authentication.UsernamePasswordAuthenticationToken("user", "pass");
        assertThat(provider.authenticate(otherAuth)).isSameAs(otherAuth);
    }

    @Test
    void supports_shouldReturnTrueForJWTAuthentication() {
        assertThat(provider.supports(JWTAuthentication.class)).isTrue();
    }

    @Test
    void supports_shouldReturnFalseForOtherClass() {
        assertThat(provider.supports(String.class)).isFalse();
    }
}


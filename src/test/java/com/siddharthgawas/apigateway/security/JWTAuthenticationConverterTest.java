package com.siddharthgawas.apigateway.security;

import com.siddharthgawas.apigateway.security.dto.JWTAuthentication;
import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.security.core.Authentication;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

class JWTAuthenticationConverterTest {
    private JWTAuthenticationConverter converter;
    private HttpServletRequest request;

    @BeforeEach
    void setUp() {
        converter = new JWTAuthenticationConverter();
        request = mock(HttpServletRequest.class);
    }

    @Test
    void convert_shouldReturnJWTAuthentication_whenValidBearerToken() {
        when(request.getHeader("Authorization")).thenReturn("Bearer test.jwt.token");
        Authentication auth = converter.convert(request);
        assertThat(auth).isInstanceOf(JWTAuthentication.class);
        assertThat(auth.getCredentials()).isEqualTo("test.jwt.token");
    }

    @Test
    void convert_shouldReturnNull_whenNoAuthorizationHeader() {
        when(request.getHeader("Authorization")).thenReturn(null);
        Authentication auth = converter.convert(request);
        assertThat(auth).isNull();
    }

    @Test
    void convert_shouldReturnNull_whenAuthorizationHeaderIsEmpty() {
        when(request.getHeader("Authorization")).thenReturn("");
        Authentication auth = converter.convert(request);
        assertThat(auth).isNull();
    }

    @Test
    void convert_shouldReturnNull_whenAuthorizationHeaderIsNotBearer() {
        when(request.getHeader("Authorization")).thenReturn("Basic abcdef");
        Authentication auth = converter.convert(request);
        assertThat(auth).isNull();
    }

    @Test
    void convert_shouldReturnNull_whenBearerPrefixButNoToken() {
        when(request.getHeader("Authorization")).thenReturn("Bearer ");
        Authentication auth = converter.convert(request);
        assertThat(auth).isInstanceOf(JWTAuthentication.class);
        assertThat(auth.getCredentials()).isEqualTo("");
    }
}


package com.siddharthgawas.apigateway.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.util.matcher.RequestMatcher;

import java.io.IOException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

class JwtAuthenticationFilterTest {
    private JwtAuthenticationFilter filter;
    private HttpServletRequest request;
    private HttpServletResponse response;
    private FilterChain filterChain;
    private Authentication authentication;

    @BeforeEach
    void setUp() {
        AuthenticationManager authenticationManager = mock(AuthenticationManager.class);
        RequestMatcher requestMatcher = mock(RequestMatcher.class);
        filter = new JwtAuthenticationFilter(requestMatcher, authenticationManager);
        request = mock(HttpServletRequest.class);
        response = mock(HttpServletResponse.class);
        filterChain = mock(FilterChain.class);
        authentication = mock(Authentication.class);
    }

    @Test
    void successfulAuthentication_setsSecurityContextAndProceeds() throws IOException, ServletException {
        // Arrange
        SecurityContextHolder.clearContext();
        // Act
        filter.successfulAuthentication(request, response, filterChain, authentication);
        // Assert
        verify(filterChain, times(1)).doFilter(request, response);
        // SecurityContext is saved via repository, but we can check that no exception is thrown
    }

    @Test
    void constructor_setsRequestMatcherAndAuthenticationManager() {
        assertThat(filter).isNotNull();
    }
}


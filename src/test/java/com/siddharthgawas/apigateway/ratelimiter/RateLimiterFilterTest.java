package com.siddharthgawas.apigateway.ratelimiter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.security.web.util.matcher.RequestMatcher;

import java.io.IOException;
import java.util.function.Function;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class RateLimiterFilterTest {
    private RateLimiterFilter filter;
    private RateLimitStrategy rateLimitStrategy;
    private Function<HttpServletRequest, String> keyExtractor;
    private jakarta.servlet.FilterChain filterChain;
    private HttpServletRequest request;
    private HttpServletResponse response;
    RequestMatcher pathMatcher = mock(RequestMatcher.class);

    @BeforeEach
    void setUp() {
        rateLimitStrategy = mock(RateLimitStrategy.class);
        keyExtractor = mock(Function.class);
        filter = new RateLimiterFilter(rateLimitStrategy, pathMatcher, keyExtractor);
        filterChain = mock(FilterChain.class);
        request = mock(HttpServletRequest.class);
        response = mock(HttpServletResponse.class);
    }

    @Test
    void doFilterInternal_shouldReturnUnauthorized_whenKeyIsMissing() throws ServletException, IOException {
        when(keyExtractor.apply(any())).thenReturn("");
        when(request.getRequestURI()).thenReturn("/api/test");
        when(response.isCommitted()).thenReturn(false);
        when(response.getWriter()).thenReturn(mock(java.io.PrintWriter.class));
        filter.doFilterInternal(request, response, filterChain);
        verify(response).setStatus(HttpStatus.UNAUTHORIZED.value());
        verify(response).getWriter();
        verifyNoInteractions(filterChain);
    }

    @Test
    void doFilterInternal_shouldReturnTooManyRequests_whenQuotaExceeded() throws ServletException, IOException {
        when(keyExtractor.apply(any())).thenReturn("user1");
        when(request.getRequestURI()).thenReturn("/api/test");
        when(rateLimitStrategy.isQuotaExceeded(any())).thenReturn(true);
        when(response.isCommitted()).thenReturn(false);
        when(response.getWriter()).thenReturn(mock(java.io.PrintWriter.class));
        filter.doFilterInternal(request, response, filterChain);
        verify(response).setStatus(HttpStatus.TOO_MANY_REQUESTS.value());
        verify(response).getWriter();
        verifyNoInteractions(filterChain);
    }

    @Test
    void doFilterInternal_shouldProceed_whenQuotaNotExceeded() throws ServletException, IOException {
        when(keyExtractor.apply(any())).thenReturn("user2");
        when(request.getRequestURI()).thenReturn("/api/test");
        when(rateLimitStrategy.isQuotaExceeded(any())).thenReturn(false);
        filter.doFilterInternal(request, response, filterChain);
        verify(filterChain).doFilter(request, response);
    }

    @Test
    void doFilterInternal_shouldNotWriteIfResponseCommitted() throws ServletException, IOException {
        when(keyExtractor.apply(any())).thenReturn("");
        when(request.getRequestURI()).thenReturn("/api/test");
        when(response.isCommitted()).thenReturn(true);
        filter.doFilterInternal(request, response, filterChain);
        verify(response, never()).setStatus(anyInt());
        verify(response, never()).getWriter();
        verifyNoInteractions(filterChain);
    }

    @Test
    void shouldNotFilter_shouldReturnFalse_whenRequestMatcherMatches() throws ServletException {
        RateLimiterFilter customFilter = new RateLimiterFilter(rateLimitStrategy, pathMatcher ,keyExtractor);
        when(pathMatcher.matches(request)).thenReturn(true);
        assertThat(customFilter.shouldNotFilter(request)).isFalse();
    }

    @Test
    void shouldNotFilter_shouldReturnTrue_whenRequestMatcherDoesNotMatch() throws ServletException {
        RateLimiterFilter customFilter = new RateLimiterFilter(rateLimitStrategy, pathMatcher, keyExtractor);
        when(pathMatcher.matches(request)).thenReturn(false);
        assertThat(customFilter.shouldNotFilter(request)).isTrue();
    }
}


package com.siddharthgawas.apigateway.ratelimiter;

import com.siddharthgawas.apigateway.ratelimiter.dto.RateLimitProps;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.NonNull;
import org.springframework.http.HttpStatus;
import org.springframework.security.web.context.RequestAttributeSecurityContextRepository;
import org.springframework.security.web.context.SecurityContextRepository;
import org.springframework.security.web.util.matcher.RequestMatcher;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.function.Function;

public class RateLimiterFilter extends OncePerRequestFilter {

    private final SecurityContextRepository securityContextRepository =
            new RequestAttributeSecurityContextRepository();

    private final RateLimitStrategy rateLimitStrategy;

    private final RequestMatcher requestMatcher;

    private final Function<HttpServletRequest, String> keyExtractor;


    public RateLimiterFilter(final RateLimitStrategy rateLimitStrategy, final RequestMatcher requestMatcher) {
        this.rateLimitStrategy = rateLimitStrategy;
        this.requestMatcher = requestMatcher;
        this.keyExtractor = ServletRequest::getRemoteAddr;
    }

    public RateLimiterFilter(final RateLimitStrategy rateLimitStrategy,
                             final RequestMatcher requestMatcher,
                             final Function<HttpServletRequest, String> keyExtractor) {
        this.rateLimitStrategy = rateLimitStrategy;
        this.requestMatcher = requestMatcher;
        this.keyExtractor = keyExtractor;
    }



    @Override
    protected void doFilterInternal(final HttpServletRequest request,
                                    final HttpServletResponse response,
                                    final FilterChain filterChain) throws ServletException, IOException {
        final var requestPath = request.getRequestURI();
        final var key = keyExtractor.apply(request);
        if (!StringUtils.hasLength(key)) {
            if (!response.isCommitted()) {
                response.setStatus(HttpStatus.UNAUTHORIZED.value());
                response.getWriter().write("Unauthorized access. Please provide valid credentials.");
            }
            return;
        }
        final var isQuotaExhausted = rateLimitStrategy.isQuotaExceeded(new RateLimitProps(key, requestPath));
        if (isQuotaExhausted) {
            if (!response.isCommitted()) {
                response.setStatus(HttpStatus.TOO_MANY_REQUESTS.value());
                response.getWriter().write("Rate limit exceeded. Please try again later.");
            }
        } else {
            filterChain.doFilter(request, response);
        }
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) throws ServletException {
        return !requestMatcher.matches(request);
    }
}

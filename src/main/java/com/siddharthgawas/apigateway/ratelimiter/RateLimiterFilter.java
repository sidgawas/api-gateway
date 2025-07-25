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

/**
 * Filter for rate limiting requests based on a specified strategy.
 * <p>
 * This filter checks if the rate limit for a request has been exceeded and
 * responds with an appropriate HTTP status code if the limit is reached.
 */
public class RateLimiterFilter extends OncePerRequestFilter {

    private final SecurityContextRepository securityContextRepository =
            new RequestAttributeSecurityContextRepository();

    private final RateLimitStrategy rateLimitStrategy;

    private final RequestMatcher requestMatcher;

    private final Function<HttpServletRequest, String> keyExtractor;


    /**
     * Constructs a RateLimiterFilter with the specified rate limit strategy and request matcher.
     *
     * @param rateLimitStrategy the strategy to use for rate limiting
     * @param requestMatcher    the matcher to determine which requests to filter
     */
    public RateLimiterFilter(final RateLimitStrategy rateLimitStrategy, final RequestMatcher requestMatcher) {
        this.rateLimitStrategy = rateLimitStrategy;
        this.requestMatcher = requestMatcher;
        this.keyExtractor = ServletRequest::getRemoteAddr;
    }

    /**
     * Constructs a RateLimiterFilter with the specified rate limit strategy, request matcher,
     * and key extractor function.
     *
     * @param rateLimitStrategy the strategy to use for rate limiting
     * @param requestMatcher    the matcher to determine which requests to filter
     * @param keyExtractor      function to extract the key from the request
     */
    public RateLimiterFilter(final RateLimitStrategy rateLimitStrategy,
                             final RequestMatcher requestMatcher,
                             final Function<HttpServletRequest, String> keyExtractor) {
        this.rateLimitStrategy = rateLimitStrategy;
        this.requestMatcher = requestMatcher;
        this.keyExtractor = keyExtractor;
    }


    /**
     * Filters incoming requests to apply rate limiting.
     * <p>
     * This method checks if the request matches the specified request matcher and
     * applies the rate limit strategy. If the rate limit is exceeded, it responds
     * with a 429 Too Many Requests status; otherwise, it allows the request to proceed.
     *
     * @param request  the HttpServletRequest to filter
     * @param response the HttpServletResponse to write the response to
     * @param filterChain the filter chain to continue processing the request
     * @throws ServletException if an error occurs during filtering
     * @throws IOException      if an I/O error occurs
     */
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

    /**
     * Determines whether the filter should be applied to the request.
     * <p>
     * This method checks if the request matches the specified request matcher.
     *
     * @param request the HttpServletRequest to check
     * @return true if the filter should not be applied, false otherwise
     * @throws ServletException if an error occurs during filtering
     */
    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) throws ServletException {
        return !requestMatcher.matches(request);
    }
}

package com.siddharthgawas.apigateway.configuration;

import com.siddharthgawas.apigateway.controller.AuthenticationController;
import com.siddharthgawas.apigateway.ratelimiter.RateLimitStrategy;
import com.siddharthgawas.apigateway.ratelimiter.RateLimiterFilter;
import com.siddharthgawas.apigateway.ratelimiter.impl.TokenBucketRateLimitStrategy;
import com.siddharthgawas.apigateway.security.JWTAuthenticationConverter;
import com.siddharthgawas.apigateway.security.JWTAuthenticationProvider;
import com.siddharthgawas.apigateway.security.JwtAuthenticationFilter;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.ProviderManager;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.HttpStatusEntryPoint;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.context.RequestAttributeSecurityContextRepository;
import org.springframework.security.web.servlet.util.matcher.PathPatternRequestMatcher;
import org.springframework.security.web.util.matcher.NegatedRequestMatcher;
import org.springframework.security.web.util.matcher.OrRequestMatcher;
import org.springframework.security.web.util.matcher.RequestMatcher;

import java.security.Principal;
import java.util.Optional;
import java.util.function.Function;
import java.util.function.Supplier;

/**
 * Security configuration for the API Gateway.
 * <p>
 * This class configures security settings, including authentication and authorization,
 * using JWT tokens and Redis for rate limiting.
 */
@Configuration
@EnableWebSecurity
public class SecurityConfiguration {

    @Autowired
    private ApplicationProperties applicationProperties;

    /**
     * Request matcher for unauthenticated requests.
     * <p>
     * This matcher allows access to the token endpoint, token refresh endpoint, and error endpoint
     * without authentication.
     */
    private final RequestMatcher unauthenticatedRequestMatcher = new OrRequestMatcher(
            PathPatternRequestMatcher.withDefaults()
                    .matcher(AuthenticationController.TOKEN_ENDPOINT),
            PathPatternRequestMatcher.withDefaults()
                    .matcher(AuthenticationController.TOKEN_REFRESH_ENDPOINT),
            PathPatternRequestMatcher.withDefaults()
                    .matcher("/error")
    );

    /**
     * Password encoder bean for encoding passwords.
     * <p>
     * This bean uses BCrypt for password encoding, which is a strong hashing algorithm.
     *
     * @return the password encoder
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    /**
     * Authentication provider for JWT authentication.
     * <p>
     * This bean is responsible for authenticating users based on JWT tokens.
     *
     * @return the JWT authentication provider
     */
    @Bean
    public AuthenticationManager authenticationManager(final JWTAuthenticationProvider authenticationProvider) {
        final var providerManager = new ProviderManager(authenticationProvider);
        providerManager.setEraseCredentialsAfterAuthentication(true);
        return providerManager;
    }

    /**
     * Security filter chain bean for configuring security settings.
     * <p>
     * This method configures HTTP security, including CSRF protection, session management,
     * exception handling, and filters for JWT authentication and rate limiting.
     *
     * @param http the HttpSecurity object
     * @param redisTemplate the RedisTemplate for rate limiting
     * @param authenticationManager the authentication manager
     * @return the configured SecurityFilterChain
     * @throws Exception if an error occurs during configuration
     */
    @Bean
    public SecurityFilterChain filterChain(final HttpSecurity http,
                                           final RedisTemplate<String, Object> redisTemplate,
                                           final AuthenticationManager authenticationManager) throws Exception {
        http.csrf(AbstractHttpConfigurer::disable)
                .authorizeHttpRequests(authorize -> authorize
                        .requestMatchers(unauthenticatedRequestMatcher).permitAll()
                        .anyRequest().authenticated()
                )
                .sessionManagement(session -> session
                        .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                )
                .exceptionHandling(customizer -> {
                    customizer.authenticationEntryPoint(new HttpStatusEntryPoint(HttpStatus.UNAUTHORIZED));
                })
                .addFilterBefore(jwtAuthenticationFilter(authenticationManager),
                        UsernamePasswordAuthenticationFilter.class)
                .addFilterAfter(getRequestRateLimiterForAuthenticatedRequests(redisTemplate),
                        JwtAuthenticationFilter.class)
                .addFilterAfter(getRequestRateLimiterForUnauthenticatedRequests(redisTemplate),
                        JwtAuthenticationFilter.class)
                .authenticationManager(authenticationManager);
        return http.build();
    }

    /**
     * JWT authentication filter bean.
     * <p>
     * This filter intercepts requests to authenticate users based on JWT tokens.
     *
     * @param authenticationManager the authentication manager
     * @return the JWT authentication filter
     */
    private JwtAuthenticationFilter jwtAuthenticationFilter(final AuthenticationManager authenticationManager) {
        final var filter = new JwtAuthenticationFilter(
                new NegatedRequestMatcher(unauthenticatedRequestMatcher),
                authenticationManager
        );
        filter.setAuthenticationConverter(new JWTAuthenticationConverter());
        return filter;
    }

    /**
     * Rate limiter filter for authenticated requests.
     * <p>
     * This filter applies rate limiting to authenticated requests using a token bucket strategy.
     *
     * @param redisTemplate the RedisTemplate for rate limiting
     * @return the rate limiter filter for authenticated requests
     */
    private RateLimiterFilter getRequestRateLimiterForAuthenticatedRequests(final RedisTemplate<String, Object> redisTemplate) {
        return new RateLimiterFilter(getTokenRateLimitStrategy(redisTemplate),
                new NegatedRequestMatcher(unauthenticatedRequestMatcher),
                getUserIDKeyExtractor());
    }

    /**
     * Rate limiter filter for unauthenticated requests.
     * <p>
     * This filter applies rate limiting to unauthenticated requests using a token bucket strategy.
     *
     * @param redisTemplate the RedisTemplate for rate limiting
     * @return the rate limiter filter for unauthenticated requests
     */
    private RateLimiterFilter getRequestRateLimiterForUnauthenticatedRequests(final RedisTemplate<String, Object> redisTemplate) {
        return new RateLimiterFilter(getTokenRateLimitStrategy(redisTemplate), unauthenticatedRequestMatcher);
    }

    /**
     * Extracts the user ID from the security context for rate limiting.
     * <p>
     * This method uses a custom key extractor to retrieve the user ID from the security context.
     *
     * @return a function that extracts the user ID from the HttpServletRequest
     */
    private Function<HttpServletRequest, String> getUserIDKeyExtractor() {
        final var securityContextRepository = new RequestAttributeSecurityContextRepository();
        // Use a custom key extractor to get the user-id from the security context
        return request -> Optional
                .of(securityContextRepository.loadDeferredContext(request))
                .map(Supplier::get)
                .map(SecurityContext::getAuthentication)
                .map(Principal::getName)
                .orElse(null);
    }

    /**
     * Token bucket rate limit strategy for Redis.
     * <p>
     * This method creates a token bucket rate limit strategy using Redis to manage request limits.
     *
     * @param redisTemplate the RedisTemplate for rate limiting
     * @return the token bucket rate limit strategy
     */
    private RateLimitStrategy getTokenRateLimitStrategy(final RedisTemplate<String, Object> redisTemplate) {
        return new TokenBucketRateLimitStrategy(redisTemplate, applicationProperties.getMaxReqPerMinute());
    }

}

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

@Configuration
@EnableWebSecurity
public class SecurityConfiguration {

    @Autowired
    private ApplicationProperties applicationProperties;

    private final RequestMatcher unauthenticatedRequestMatcher = new OrRequestMatcher(
            PathPatternRequestMatcher.withDefaults()
                    .matcher(AuthenticationController.TOKEN_ENDPOINT),
            PathPatternRequestMatcher.withDefaults()
                    .matcher(AuthenticationController.TOKEN_REFRESH_ENDPOINT),
            PathPatternRequestMatcher.withDefaults()
                    .matcher("/error")
    );

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public AuthenticationManager authenticationManager(final JWTAuthenticationProvider authenticationProvider) {
        final var providerManager = new ProviderManager(authenticationProvider);
        providerManager.setEraseCredentialsAfterAuthentication(true);
        return providerManager;
    }

    @Bean
    public SecurityFilterChain filterChain(final HttpSecurity http,
                                           final RedisTemplate<String, Number> redisTemplate,
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

    private JwtAuthenticationFilter jwtAuthenticationFilter(final AuthenticationManager authenticationManager) {
        final var filter = new JwtAuthenticationFilter(
                new NegatedRequestMatcher(unauthenticatedRequestMatcher),
                authenticationManager
        );
        filter.setAuthenticationConverter(new JWTAuthenticationConverter());
        return filter;
    }

    private RateLimiterFilter getRequestRateLimiterForAuthenticatedRequests(final RedisTemplate<String, Number> redisTemplate) {
        return new RateLimiterFilter(getTokenRateLimitStrategy(redisTemplate),
                new NegatedRequestMatcher(unauthenticatedRequestMatcher),
                getUserIDKeyExtractor());
    }

    private RateLimiterFilter getRequestRateLimiterForUnauthenticatedRequests(final RedisTemplate<String, Number> redisTemplate) {
        return new RateLimiterFilter(getTokenRateLimitStrategy(redisTemplate), unauthenticatedRequestMatcher);
    }

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

    private RateLimitStrategy getTokenRateLimitStrategy(final RedisTemplate<String, Number> redisTemplate) {
        return new TokenBucketRateLimitStrategy(redisTemplate, applicationProperties.getMaxReqPerMinute());
    }

}

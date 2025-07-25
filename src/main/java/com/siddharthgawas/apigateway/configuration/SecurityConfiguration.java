package com.siddharthgawas.apigateway.configuration;

import com.siddharthgawas.apigateway.controller.AuthenticationController;
import com.siddharthgawas.apigateway.security.JWTAuthenticationConverter;
import com.siddharthgawas.apigateway.security.JWTAuthenticationProvider;
import com.siddharthgawas.apigateway.security.JwtAuthenticationFilter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.ProviderManager;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.HttpStatusEntryPoint;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.servlet.util.matcher.PathPatternRequestMatcher;
import org.springframework.security.web.util.matcher.NegatedRequestMatcher;
import org.springframework.security.web.util.matcher.OrRequestMatcher;

@Configuration
@EnableWebSecurity
public class SecurityConfiguration {

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
                                           final AuthenticationManager authenticationManager) throws Exception {
        http.csrf(AbstractHttpConfigurer::disable)
                .authorizeHttpRequests(authorize -> authorize
                        .requestMatchers(AuthenticationController.TOKEN_ENDPOINT,
                                "/error",
                                AuthenticationController.TOKEN_REFRESH_ENDPOINT).permitAll()
                        .anyRequest().authenticated()
                )
                .sessionManagement(session -> session
                        .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                )
                .exceptionHandling(customizer -> {
                    customizer.authenticationEntryPoint(new HttpStatusEntryPoint(HttpStatus.UNAUTHORIZED));
                })
                .addFilterBefore(jwtAuthenticationFilter(authenticationManager), UsernamePasswordAuthenticationFilter.class)
                .authenticationManager(authenticationManager);
        return http.build();
    }

    private JwtAuthenticationFilter jwtAuthenticationFilter(final AuthenticationManager authenticationManager) {
        final var pathMatcher = new OrRequestMatcher(
                PathPatternRequestMatcher.withDefaults()
                        .matcher(AuthenticationController.TOKEN_ENDPOINT),
                PathPatternRequestMatcher.withDefaults()
                        .matcher(AuthenticationController.TOKEN_REFRESH_ENDPOINT),
                PathPatternRequestMatcher.withDefaults()
                        .matcher("/error")
        );
        final var filter = new JwtAuthenticationFilter(
                new NegatedRequestMatcher(pathMatcher),
                authenticationManager
        );
        filter.setAuthenticationConverter(new JWTAuthenticationConverter());
        return filter;
    }

}

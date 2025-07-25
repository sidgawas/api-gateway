package com.siddharthgawas.apigateway.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.context.SecurityContextHolderStrategy;
import org.springframework.security.web.authentication.AbstractAuthenticationProcessingFilter;
import org.springframework.security.web.context.RequestAttributeSecurityContextRepository;
import org.springframework.security.web.context.SecurityContextRepository;
import org.springframework.security.web.util.matcher.RequestMatcher;

import java.io.IOException;

/**
 * JwtAuthenticationFilter is responsible for processing JWT authentication requests.
 * It extends AbstractAuthenticationProcessingFilter to handle the authentication logic
 * and save the security context after successful authentication.
 */
public class JwtAuthenticationFilter extends AbstractAuthenticationProcessingFilter {

    private final SecurityContextHolderStrategy securityContextHolderStrategy =
            SecurityContextHolder.getContextHolderStrategy();
    private final SecurityContextRepository securityContextRepository =
            new RequestAttributeSecurityContextRepository();


    public JwtAuthenticationFilter(RequestMatcher requiresAuthenticationRequestMatcher,
                                      AuthenticationManager authenticationManager) {
        super(requiresAuthenticationRequestMatcher, authenticationManager);
    }

    /**
     * Attempts to authenticate the request using the provided authentication manager.
     * If successful, it saves the security context and continues the filter chain.
     *
     * @param request  the HTTP request
     * @param response the HTTP response
     * @param chain    the filter chain
     * @throws IOException      if an I/O error occurs
     * @throws ServletException if a servlet error occurs
     */
    @Override
    protected void successfulAuthentication(HttpServletRequest request, HttpServletResponse response,
                                            FilterChain chain, Authentication authResult) throws IOException,
            ServletException {
        final var securityContext = securityContextHolderStrategy.createEmptyContext();
        securityContext.setAuthentication(authResult);
        securityContextRepository.saveContext(securityContext, request, response);
        chain.doFilter(request, response);
    }
}

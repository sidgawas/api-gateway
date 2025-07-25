package com.siddharthgawas.apigateway.security;

import com.siddharthgawas.apigateway.security.dto.JWTAuthentication;
import com.siddharthgawas.apigateway.service.JWTService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.AuthenticationServiceException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.stereotype.Component;

import java.util.Objects;

@Component
@Slf4j
public class JWTAuthenticationProvider implements AuthenticationProvider {

    private final JWTService jwtService;

    @Autowired
    public JWTAuthenticationProvider(JWTService jwtService) {
        this.jwtService = jwtService;
    }

    @Override
    public Authentication authenticate(Authentication authentication) throws AuthenticationException {
        if (authentication instanceof JWTAuthentication jwtAuthentication) {
            var token = (String) jwtAuthentication.getCredentials();
            var isTokenValid = jwtService.validateToken(token);
            if (!isTokenValid) {
                log.warn("Invalid access token received");
                throw new AuthenticationServiceException("Invalid access token");
            }
            var userDetails = jwtService.getUserFromToken(token);
            if (Objects.isNull(userDetails)) {
                log.warn("No user found for the provided access token");
                throw new AuthenticationServiceException("No user found for the provided access token");
            }
            jwtAuthentication.setUserDetails(userDetails);
            jwtAuthentication.setAuthenticated(true);
            return jwtAuthentication;
        }
        return authentication;
    }

    @Override
    public boolean supports(Class<?> authentication) {
        return JWTAuthentication.class.equals(authentication);
    }
}

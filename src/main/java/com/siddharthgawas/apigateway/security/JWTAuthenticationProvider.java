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

/**
 * JWTAuthenticationProvider is responsible for authenticating JWT tokens.
 * It implements the AuthenticationProvider interface to provide custom authentication logic.
 */
@Component
@Slf4j
public class JWTAuthenticationProvider implements AuthenticationProvider {

    private final JWTService jwtService;

    @Autowired
    public JWTAuthenticationProvider(JWTService jwtService) {
        this.jwtService = jwtService;
    }

    /**
     * Authenticates the provided JWTAuthentication object.
     * It checks if the token is valid and retrieves the user details from the token.
     *
     * @param authentication the JWTAuthentication object containing the token
     * @return an authenticated JWTAuthentication object with user details
     * @throws AuthenticationServiceException if the token is invalid or no user is found
     */
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

    /**
     * Indicates whether this provider supports the given authentication type.
     *
     * @param authentication the class of the authentication object
     * @return true if the provider supports JWTAuthentication, false otherwise
     */
    @Override
    public boolean supports(Class<?> authentication) {
        return JWTAuthentication.class.equals(authentication);
    }
}

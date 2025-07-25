package com.siddharthgawas.apigateway.security.dto;

import lombok.Setter;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.List;

/**
 * JWTAuthentication represents an authentication token that contains a JWT token.
 * It extends AbstractAuthenticationToken to provide the necessary methods for
 * authentication in Spring Security.
 */
public class JWTAuthentication extends AbstractAuthenticationToken {

    private final String token;

    @Setter
    private UserDetails userDetails = null;

    public JWTAuthentication(String token) {
        super(List.of(new SimpleGrantedAuthority("ROLE_USER")));
        this.token = token;
        setAuthenticated(false);
    }

    @Override
    public Object getCredentials() {
        return this.token;
    }

    @Override
    public Object getPrincipal() {
        return this.userDetails;
    }

    @Override
    public String getName() {
        return this.userDetails != null ? this.userDetails.getUsername() : null;
    }
}

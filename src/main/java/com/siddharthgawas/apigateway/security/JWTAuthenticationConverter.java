package com.siddharthgawas.apigateway.security;

import com.siddharthgawas.apigateway.security.dto.JWTAuthentication;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.AuthenticationConverter;
import org.springframework.util.StringUtils;


/**
 * JWTAuthenticationConverter is responsible for converting an HTTP request into a JWTAuthentication object.
 * It extracts the JWT token from the "Authorization" header of the request.
 */
public class JWTAuthenticationConverter implements AuthenticationConverter {
    @Override
    public Authentication convert(HttpServletRequest request) {
        var tokenHeader = request.getHeader("Authorization");
        if (!StringUtils.hasLength(tokenHeader) || !tokenHeader.startsWith("Bearer ")) {
            return null;
        }
        return new JWTAuthentication(tokenHeader.substring(7));
    }
}

package com.siddharthgawas.apigateway.service;

import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;

/**
 * JWTService is an interface that defines methods for generating and validating JWT tokens.
 * It provides methods to generate access and refresh tokens, validate tokens, and retrieve user details from a token.
 */
public interface JWTService {

    String generateAccessToken(final UserDetails userDetails);

    String generateRefreshToken(final UserDetails userDetails);

    Boolean validateToken(String token);

    User getUserFromToken(String token);
}

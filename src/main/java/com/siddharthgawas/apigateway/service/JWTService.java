package com.siddharthgawas.apigateway.service;

import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;

public interface JWTService {

    String generateAccessToken(final UserDetails userDetails);

    String generateRefreshToken(final UserDetails userDetails);

    Boolean validateToken(String token);

    User getUserFromToken(String token);
}

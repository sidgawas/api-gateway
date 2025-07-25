package com.siddharthgawas.apigateway.service.impl;

import com.siddharthgawas.apigateway.security.dto.TokenDetails;
import com.siddharthgawas.apigateway.service.AuthenticationService;
import com.siddharthgawas.apigateway.service.JWTService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * AuthenticationServiceImpl is responsible for handling user authentication and token generation.
 * It implements the AuthenticationService interface to provide custom authentication logic.
 */
@Service
public class AuthenticationServiceImpl implements AuthenticationService {

    private final Map<String, User> users;

    private final PasswordEncoder passwordEncoder;

    private final JWTService jwtService;

    @Autowired
    public AuthenticationServiceImpl(final PasswordEncoder passwordEncoder, final JWTService jwtService) {
        this.jwtService = jwtService;
        this.users = new HashMap<>();
        this.users.put("johndoe", new User("johndoe", passwordEncoder.encode("johndoe"),
                Collections.emptyList()));
        this.users.put("janedoe", new User("janedoe", passwordEncoder.encode("janedoe"),
                Collections.emptyList()));
        this.passwordEncoder = passwordEncoder;


    }

    /**
     * Authenticates a user with the provided username and password.
     * If the credentials are valid, it generates access and refresh tokens.
     *
     * @param username the username of the user
     * @param password the password of the user
     * @return TokenDetails containing access and refresh tokens
     * @throws AccessDeniedException if the username or password is invalid
     */
    @Override
    public TokenDetails authenticate(final String username, final String password) {
        var user = this.users.get(username);
        if (Objects.isNull(user) || !this.passwordEncoder.matches(password, user.getPassword())) {
            throw new AccessDeniedException("Invalid username or password");
        }
        var accessToken = this.jwtService.generateAccessToken(user);
        var refreshToken = this.jwtService.generateRefreshToken(user);
        return new TokenDetails(accessToken, refreshToken);
    }

    /**
     * Refreshes the access token using the provided refresh token.
     * Validates the refresh token and generates a new access token if valid.
     *
     * @param refreshToken the refresh token to validate and use for generating a new access token
     * @return TokenDetails containing the new access token and a new refresh token
     * @throws AccessDeniedException if the refresh token is invalid or no user is found
     */
    @Override
    public TokenDetails refreshAccessToken(final String refreshToken) {
        var isValid = this.jwtService.validateToken(refreshToken);
        if (!isValid) {
            throw new AccessDeniedException("Invalid refresh token");
        }
        var user = this.jwtService.getUserFromToken(refreshToken);
        if (Objects.isNull(user)) {
            throw new AccessDeniedException("No user found for the provided refresh token");
        }
        var accessToken = this.jwtService.generateAccessToken(user);
        return new TokenDetails(accessToken, this.jwtService.generateRefreshToken(user));
    }
}

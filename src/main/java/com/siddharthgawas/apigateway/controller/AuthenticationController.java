package com.siddharthgawas.apigateway.controller;

import com.siddharthgawas.apigateway.dto.TokenRefreshRequest;
import com.siddharthgawas.apigateway.security.dto.TokenDetails;
import com.siddharthgawas.apigateway.service.AuthenticationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

/**
 * Controller for handling authentication-related requests.
 * <p>
 * This controller provides endpoints for generating and refreshing authentication tokens.
 */
@RestController
public class AuthenticationController {

    public static final String TOKEN_ENDPOINT = "/token";

    public static final String TOKEN_REFRESH_ENDPOINT = "/token-refresh";

    private static final String REFRESH_TOKEN_COOKIE_NAME = "refreshToken";

    private final AuthenticationService authenticationService;


    @Autowired
    public AuthenticationController(AuthenticationService authenticationService) {
        this.authenticationService = authenticationService;
    }

    /**
     * Endpoint to generate a new authentication token.
     * <p>
     * This endpoint accepts a username and password, authenticates the user, and returns a token.
     *
     * @param username the username of the user
     * @param password the password of the user
     * @return a ResponseEntity containing the generated token details
     */
    @PostMapping(TOKEN_ENDPOINT)
    public ResponseEntity<TokenDetails> generateToken(@RequestParam String username,
                                                      @RequestParam String password) {
        final var tokenDetails = this.authenticationService.authenticate(username, password);
        final var refreshCookie = getResponseCookie(tokenDetails);
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .header(HttpHeaders.SET_COOKIE, refreshCookie.toString())
                .body(tokenDetails);
    }

    /**
     * Endpoint to refresh an existing authentication token.
     * <p>
     * This endpoint accepts a refresh token and returns a new access token.
     *
     * @param request the request containing the refresh token
     * @param refreshToken the refresh token from the cookie
     * @return a ResponseEntity containing the refreshed token details
     */
    @PostMapping(TOKEN_REFRESH_ENDPOINT)
    public ResponseEntity<TokenDetails> refreshToken(@RequestBody(required = false) TokenRefreshRequest request,
                                                     @CookieValue(REFRESH_TOKEN_COOKIE_NAME) String refreshToken) {
        final var tokenResponse = this.authenticationService.refreshAccessToken(Optional.ofNullable(refreshToken)
                .filter(StringUtils::hasLength)
                .orElse(Optional.ofNullable(request).map(TokenRefreshRequest::refreshToken).orElse("")));
        final var refreshCookie = getResponseCookie(tokenResponse);
        return ResponseEntity.status(HttpStatus.CREATED)
                .header(HttpHeaders.SET_COOKIE, refreshCookie.toString())
                .body(tokenResponse);
    }

    /**
     * Creates a response cookie for the refresh token.
     * <p>
     * This method creates a secure, HTTP-only cookie for the refresh token.
     *
     * @param tokenDetails the token details containing the refresh token
     * @return a ResponseCookie for the refresh token
     */
    private ResponseCookie getResponseCookie(TokenDetails tokenDetails) {
        return ResponseCookie.from(REFRESH_TOKEN_COOKIE_NAME, tokenDetails.refreshToken())
                .httpOnly(true)
                .secure(true)
                .path(TOKEN_REFRESH_ENDPOINT)
                .build();
    }
}
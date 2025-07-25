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

    private ResponseCookie getResponseCookie(TokenDetails tokenDetails) {
        return ResponseCookie.from(REFRESH_TOKEN_COOKIE_NAME, tokenDetails.refreshToken())
                .httpOnly(true)
                .secure(true)
                .path(TOKEN_REFRESH_ENDPOINT)
                .build();
    }
}
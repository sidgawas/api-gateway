package com.siddharthgawas.apigateway.controller;

import com.siddharthgawas.apigateway.dto.TokenRefreshRequest;
import com.siddharthgawas.apigateway.security.dto.TokenDetails;
import com.siddharthgawas.apigateway.service.AuthenticationService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class AuthenticationControllerTest {
    private AuthenticationService authenticationService;
    private AuthenticationController authenticationController;

    @BeforeEach
    void setUp() {
        authenticationService = mock(AuthenticationService.class);
        authenticationController = new AuthenticationController(authenticationService);
    }

    @Test
    void generateToken_shouldReturnTokenDetailsAndSetCookie() {
        String username = "user";
        String password = "pass";
        TokenDetails tokenDetails = new TokenDetails("access", "refresh");
        when(authenticationService.authenticate(username, password)).thenReturn(tokenDetails);

        ResponseEntity<TokenDetails> response = authenticationController.generateToken(username, password);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(response.getHeaders().getFirst(HttpHeaders.SET_COOKIE)).contains("refresh");
        assertThat(response.getBody()).isEqualTo(tokenDetails);
    }

    @Test
    void refreshToken_shouldReturnTokenDetailsAndSetCookie_whenCookiePresent() {
        String refreshToken = "refreshToken";
        TokenDetails tokenDetails = new TokenDetails("access", refreshToken);
        when(authenticationService.refreshAccessToken(refreshToken)).thenReturn(tokenDetails);

        ResponseEntity<TokenDetails> response = authenticationController.refreshToken(null, refreshToken);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(response.getHeaders().getFirst(HttpHeaders.SET_COOKIE)).contains(refreshToken);
        assertThat(response.getBody()).isEqualTo(tokenDetails);
    }

    @Test
    void refreshToken_shouldReturnTokenDetailsAndSetCookie_whenRequestBodyPresent() {
        String refreshToken = "refreshToken";
        TokenRefreshRequest request = new TokenRefreshRequest(refreshToken);
        TokenDetails tokenDetails = new TokenDetails("access", refreshToken);
        when(authenticationService.refreshAccessToken(refreshToken)).thenReturn(tokenDetails);

        ResponseEntity<TokenDetails> response = authenticationController.refreshToken(request, null);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(response.getHeaders().getFirst(HttpHeaders.SET_COOKIE)).contains(refreshToken);
        assertThat(response.getBody()).isEqualTo(tokenDetails);
    }

    @Test
    void refreshToken_shouldReturnTokenDetailsAndSetCookie_whenBothNull() {
        TokenDetails tokenDetails = new TokenDetails("access", "");
        when(authenticationService.refreshAccessToken("")).thenReturn(tokenDetails);

        ResponseEntity<TokenDetails> response = authenticationController.refreshToken(null, null);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(response.getHeaders().getFirst(HttpHeaders.SET_COOKIE)).contains("");
        assertThat(response.getBody()).isEqualTo(tokenDetails);
    }
}


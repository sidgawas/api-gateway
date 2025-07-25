package com.siddharthgawas.apigateway.service;

import com.siddharthgawas.apigateway.security.dto.TokenDetails;

/**
 * Service interface for handling authentication operations.
 * <p>
 * This interface defines methods for user authentication and token management,
 * including generating access tokens and refreshing them.
 */
public interface AuthenticationService {

    TokenDetails authenticate(String username, String password);

    TokenDetails refreshAccessToken(String refreshToken);
}

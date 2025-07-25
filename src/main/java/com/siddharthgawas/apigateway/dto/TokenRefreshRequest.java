package com.siddharthgawas.apigateway.dto;

/**
 * Represents a request to refresh an authentication token.
 * <p>
 * This record is used to encapsulate the refresh token required for refreshing
 * the access token in the API Gateway.
 *
 * @param refreshToken The refresh token used to obtain a new access token.
 */
public record TokenRefreshRequest(
        String refreshToken
) {

}

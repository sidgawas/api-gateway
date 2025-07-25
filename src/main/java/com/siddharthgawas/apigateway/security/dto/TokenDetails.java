package com.siddharthgawas.apigateway.security.dto;

/**
 * Represents the details of authentication tokens.
 * <p>
 * This record is used to encapsulate the access token and refresh token
 * returned by the authentication service.
 *
 * @param accessToken  The access token used for authentication.
 * @param refreshToken The refresh token used to obtain a new access token.
 */
public record TokenDetails(String accessToken, String refreshToken) {
}
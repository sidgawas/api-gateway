package com.siddharthgawas.apigateway.dto;

/**
 * Represents a generic API response with a message.
 * <p>
 * This record is used to encapsulate the response message returned by the API.
 *
 * @param message The message to be included in the API response.
 */
public record APIResponse(String message) {
}

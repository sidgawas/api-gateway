package com.siddharthgawas.apigateway.ratelimiter.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

/**
 * Represents the properties required for rate limiting.
 * <p>
 * This class encapsulates the key and request path used for rate limiting
 * in the API Gateway.
 */
@Data
@AllArgsConstructor
@Builder
public class RateLimitProps {
    private String key;
    private String requestPath;
}

package com.siddharthgawas.apigateway.ratelimiter;

import com.siddharthgawas.apigateway.ratelimiter.dto.RateLimitProps;

/**
 * Interface for defining a rate limit strategy.
 * <p>
 * This interface provides a method to check if the quota for a given rate limit
 * has been exceeded based on the provided properties.
 */
public interface RateLimitStrategy {
    Boolean isQuotaExceeded(RateLimitProps rateLimitProps);
}

package com.siddharthgawas.apigateway.ratelimiter;

import com.siddharthgawas.apigateway.ratelimiter.dto.RateLimitProps;

public interface RateLimitStrategy {
    Boolean isQuotaExceeded(RateLimitProps rateLimitProps);
}

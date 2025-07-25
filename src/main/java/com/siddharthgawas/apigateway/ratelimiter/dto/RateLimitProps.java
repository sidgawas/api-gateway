package com.siddharthgawas.apigateway.ratelimiter.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@Data
@AllArgsConstructor
@Builder
public class RateLimitProps {
    private String key;
    private String requestPath;
}

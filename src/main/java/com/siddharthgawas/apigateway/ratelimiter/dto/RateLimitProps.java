package com.siddharthgawas.apigateway.ratelimiter.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class RateLimitProps {
    private String key;
    private String requestPath;
}

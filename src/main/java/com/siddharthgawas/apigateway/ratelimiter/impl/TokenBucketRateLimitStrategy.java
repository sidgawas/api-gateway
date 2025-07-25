package com.siddharthgawas.apigateway.ratelimiter.impl;

import com.siddharthgawas.apigateway.ratelimiter.RateLimitStrategy;
import com.siddharthgawas.apigateway.ratelimiter.dto.RateLimitProps;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;

import java.time.Duration;

@Slf4j
public class TokenBucketRateLimitStrategy implements RateLimitStrategy {

    private static final  int WINDOW_SIZE = 60; // seconds

    private final RedisTemplate<String, Number> redisTemplate;

    private final Long maxTokenPerMinute;

    private final Float refillRate;


    public TokenBucketRateLimitStrategy(RedisTemplate<String, Number> redisTemplate, Long maxTokenPerMinute) {
        this.redisTemplate = redisTemplate;
        this.maxTokenPerMinute = maxTokenPerMinute;
        this.refillRate = ((float)maxTokenPerMinute) / WINDOW_SIZE; // tokens per second
    }

    @Override
    public Boolean isQuotaExceeded(final RateLimitProps rateLimitProps) {
        var key = rateLimitProps.getKey();
        var requestPath = rateLimitProps.getRequestPath();
        var tokenKey = key + ":" + requestPath + ":tokens";
        var lastRefillKey = key + ":" + requestPath + ":lastRefill";
        var tokens = redisTemplate.opsForValue().get(tokenKey);
        var lastRefill = redisTemplate.opsForValue().get(lastRefillKey);
        log.info("Checking rate limit for key: {}, requestPath: {}, tokens: {}, lastRefill: {}",
                 key, requestPath, tokens, lastRefill);
        var currentTime = System.currentTimeMillis();
        if (tokens == null || lastRefill == null) {
            tokens = this.maxTokenPerMinute;
            lastRefill = currentTime;
            redisTemplate.opsForValue().set(tokenKey, tokens, Duration.ofSeconds(WINDOW_SIZE));
            redisTemplate.opsForValue().set(lastRefillKey, lastRefill, Duration.ofSeconds(WINDOW_SIZE));
        }
        var isQuotaExceeded = tokens.longValue() <= 0;
        if (!isQuotaExceeded) {
            redisTemplate.opsForValue().decrement(tokenKey);
        }
        return isQuotaExceeded;
    }

}

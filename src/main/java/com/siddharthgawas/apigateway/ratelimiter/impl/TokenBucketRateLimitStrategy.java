package com.siddharthgawas.apigateway.ratelimiter.impl;

import com.siddharthgawas.apigateway.ratelimiter.RateLimitStrategy;
import com.siddharthgawas.apigateway.ratelimiter.dto.RateLimitProps;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;

import java.util.List;

@Slf4j
public class TokenBucketRateLimitStrategy implements RateLimitStrategy {

    private static final String LUA_SCRIPT = """
            local tokens = tonumber(redis.call('get', KEYS[1]))
            local lastRefill = tonumber(redis.call('get', KEYS[2]))
            local currentTime = tonumber(ARGV[1])
            if not tokens or not lastRefill then
                tokens = ARGV[2]
                lastRefill = currentTime
                redis.call('set', KEYS[1], tokens, 'EX', ARGV[3])
                redis.call('set', KEYS[2], lastRefill, 'EX', ARGV[3])
            end
            local isQuotaExceeded = tonumber(tokens) <= 0
            if not isQuotaExceeded then
                redis.call('decr', KEYS[1])
                return 0
            end
            return 1
            """;

    private static final int WINDOW_SIZE = 60; // seconds

    private final RedisTemplate<String, Object> redisTemplate;

    private final Long maxTokenPerMinute;


    public TokenBucketRateLimitStrategy(RedisTemplate<String, Object> redisTemplate, Long maxTokenPerMinute) {
        this.redisTemplate = redisTemplate;
        this.maxTokenPerMinute = maxTokenPerMinute;
    }

    @Override
    public Boolean isQuotaExceeded(final RateLimitProps rateLimitProps) {
        final String key = rateLimitProps.getKey() + ":" + rateLimitProps.getRequestPath();
        final String tokenCountKey = key + ":tokens";
        final String lastRefillKey = key + ":lastRefill";
        final long currentTime = System.currentTimeMillis() / 1000; // Current time in seconds
        Long result = redisTemplate.execute(new DefaultRedisScript<>(LUA_SCRIPT, Long.class),
                List.of(tokenCountKey, lastRefillKey),
                currentTime,
                maxTokenPerMinute,
                WINDOW_SIZE);
        return result == 1L;
    }

}

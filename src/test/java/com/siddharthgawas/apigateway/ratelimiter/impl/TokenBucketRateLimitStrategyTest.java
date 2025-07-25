package com.siddharthgawas.apigateway.ratelimiter.impl;

import com.siddharthgawas.apigateway.ratelimiter.dto.RateLimitProps;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class TokenBucketRateLimitStrategyTest {
    private RedisTemplate<String, Object> redisTemplate;
    private TokenBucketRateLimitStrategy strategy;
    private final long maxTokenPerMinute = 10L;

    @BeforeEach
    void setUp() {
        redisTemplate = mock(RedisTemplate.class);
        strategy = new TokenBucketRateLimitStrategy(redisTemplate, maxTokenPerMinute);
    }

    @Test
    void isQuotaExceeded_shouldReturnFalse_whenTokensAvailable() {
        RateLimitProps props = new RateLimitProps(
                "user1",
                "/api/test"
        );
        when(redisTemplate.execute(any(DefaultRedisScript.class), anyList(), any(), any(), any()))
                .thenReturn(0L);
        boolean result = strategy.isQuotaExceeded(props);
        assertThat(result).isFalse();
    }

    @Test
    void isQuotaExceeded_shouldReturnTrue_whenTokensNotAvailable() {
        RateLimitProps props = RateLimitProps.builder()
                .key("user2")
                .requestPath("/api/test")
                .build();
        when(redisTemplate.execute(any(DefaultRedisScript.class), anyList(), any(), any(), any()))
                .thenReturn(1L);
        boolean result = strategy.isQuotaExceeded(props);
        assertThat(result).isTrue();
    }

    @Test
    void isQuotaExceeded_shouldPassCorrectKeysAndArgsToRedis() {
        RateLimitProps props = RateLimitProps.builder()
                .key("user3")
                .requestPath("/api/path")
                .build();
        when(redisTemplate.execute(any(DefaultRedisScript.class), anyList(), any(), any(), any()))
                .thenReturn(0L);
        strategy.isQuotaExceeded(props);
        ArgumentCaptor<List> keysCaptor = ArgumentCaptor.forClass(List.class);
        verify(redisTemplate).execute(any(DefaultRedisScript.class), keysCaptor.capture(), any(), any(), any());
        List<String> keys = keysCaptor.getValue();
        assertThat(keys.get(0)).contains("user3:/api/path:tokens");
        assertThat(keys.get(1)).contains("user3:/api/path:lastRefill");
    }
}


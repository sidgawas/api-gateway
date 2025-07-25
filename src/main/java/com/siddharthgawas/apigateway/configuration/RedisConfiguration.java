package com.siddharthgawas.apigateway.configuration;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;

/**
 * Configuration class for Redis.
 * <p>
 * This class sets up a RedisTemplate for serializing keys as strings and values as JSON.
 */
@Configuration
public class RedisConfiguration {

    @Bean
    public RedisTemplate<String, Object> numberRedisTemplate(final RedisConnectionFactory connectionFactory) {
        var redisTemplate = new RedisTemplate<String, Object>();
        configureRedisTemplate(connectionFactory, redisTemplate);
        return redisTemplate;
    }

    private void configureRedisTemplate(RedisConnectionFactory connectionFactory, RedisTemplate<?, ?> redisTemplate) {
        redisTemplate.setConnectionFactory(connectionFactory);
        redisTemplate.setKeySerializer(new StringRedisSerializer());
        redisTemplate.setValueSerializer(new GenericJackson2JsonRedisSerializer());
    }
}

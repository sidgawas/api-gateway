package com.siddharthgawas.apigateway.configuration;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;

@Configuration
public class RedisConfiguration {

    @Bean
    public RedisTemplate<String, Number> numberRedisTemplate(final RedisConnectionFactory connectionFactory) {
        var redisTemplate = new RedisTemplate<String, Number>();
        configureRedisTemplate(connectionFactory, redisTemplate);
        return redisTemplate;
    }

    private void configureRedisTemplate(RedisConnectionFactory connectionFactory, RedisTemplate<?, ?> redisTemplate) {
        redisTemplate.setConnectionFactory(connectionFactory);
        redisTemplate.setKeySerializer(new StringRedisSerializer());
        redisTemplate.setValueSerializer(new GenericJackson2JsonRedisSerializer());
    }
}

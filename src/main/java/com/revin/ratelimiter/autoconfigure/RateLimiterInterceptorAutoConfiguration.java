package com.revin.ratelimiter.autoconfigure;

import com.revin.ratelimiter.interceptor.RateLimitInterceptor;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.core.RedisTemplate;

@Configuration
public class RateLimiterInterceptorAutoConfiguration {

    @Bean
    public RateLimitInterceptor rateLimitInterceptor(@Qualifier("redisTemplate") RedisTemplate<String,String> redisTemplate) {
        return new RateLimitInterceptor(redisTemplate);
    }
}

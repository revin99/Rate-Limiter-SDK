package com.revin.ratelimiter.autoconfigure;

import com.revin.ratelimiter.core.FixedWindowRateLimiterService;
import com.revin.ratelimiter.core.RateLimiterService;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.core.RedisTemplate;

@Configuration
public class RateLimiterServiceAutoConfiguration {

    @Bean
    public RateLimiterService rateLimiterService(@Qualifier("redisTemplate") RedisTemplate<String, String> redisTemplate
    ) {
        return new FixedWindowRateLimiterService(redisTemplate);
    }
}

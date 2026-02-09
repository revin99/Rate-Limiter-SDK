package com.revin.ratelimiter.core;

import com.revin.ratelimiter.context.RateLimitContext;
import com.revin.ratelimiter.key.RateLimitKeyResolver;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;


public class TokenBucketRateLimiterService implements RateLimiterService{

    private final RedisTemplate<String, String> redisTemplate;
    private final RateLimitKeyResolver keyResolver;


    public TokenBucketRateLimiterService(RedisTemplate<String, String> redisTemplate, RateLimitKeyResolver keyResolver) {
        this.redisTemplate = redisTemplate;
        this.keyResolver = keyResolver;
    }

    @Override
    public boolean isAllowed(RateLimitContext context, HttpServletRequest request) {

        String key = keyResolver.resolve(context,request);

        return true;
    }
}

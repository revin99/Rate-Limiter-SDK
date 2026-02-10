package com.revin.ratelimiter.core;

import com.revin.ratelimiter.context.RateLimitContext;
import com.revin.ratelimiter.key.RateLimitKeyResolver;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;


public class LeakyBucketRateLimiterService implements RateLimiterService{


    private final RedisTemplate<String, String> redisTemplate;
    private final RateLimitKeyResolver keyResolver;

    public LeakyBucketRateLimiterService(RedisTemplate<String, String> redisTemplate, RateLimitKeyResolver keyResolver) {
        this.redisTemplate = redisTemplate;
        this.keyResolver = keyResolver;
    }

    @Override
    public RateLimitResult isAllowed(RateLimitContext context, HttpServletRequest request) {

        String key = keyResolver.resolve(context,request);

        return new RateLimitResult(true,0,0);
    }
}

package com.revin.ratelimiter.core;

import com.revin.ratelimiter.context.RateLimitContext;
import com.revin.ratelimiter.key.RateLimitKeyResolver;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.script.RedisScript;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.List;


public class FixedWindowRateLimiterService implements RateLimiterService {

    private final RedisTemplate<String,String> redisTemplate;
    private final RateLimitKeyResolver keyResolver;
    private final RedisScript<Long> fixedWindowLua;

    public FixedWindowRateLimiterService(
            @Qualifier("redisTemplate")RedisTemplate<String, String> redisTemplate,
            RateLimitKeyResolver keyResolver,
            RedisScript<Long> fixedWindowLua
    ) {
        this.redisTemplate = redisTemplate;
        this.keyResolver = keyResolver;
        this.fixedWindowLua = fixedWindowLua;
    }

    @Override
    public RateLimitResult isAllowed(RateLimitContext context, HttpServletRequest request) {

        String key = keyResolver.resolve(context,request);

        Long result = redisTemplate.execute(
                fixedWindowLua,
                List.of(key),
                String.valueOf(context.getRateLimit().limit()),
                String.valueOf(context.getRateLimit().durationSeconds())
        );
        boolean allowed = result!=null && result>=0;

        long limit = context.getRateLimit().limit();
        long remaining = allowed?Math.max(0,limit-result):0;

        Long ttlSeconds = redisTemplate.getExpire(key);

        long nowEpochSeconds = System.currentTimeMillis() / 1000;

        Long remainingWindowSeconds = (ttlSeconds != null && ttlSeconds > 0) ? ttlSeconds : 0;

        long resetEpochSeconds =
                nowEpochSeconds + remainingWindowSeconds;

        return new RateLimitResult(
                allowed,
                remaining,
                resetEpochSeconds
        );


    }
}

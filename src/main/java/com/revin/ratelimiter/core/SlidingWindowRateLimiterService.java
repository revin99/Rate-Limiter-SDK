package com.revin.ratelimiter.core;

import com.revin.ratelimiter.context.RateLimitContext;
import com.revin.ratelimiter.key.RateLimitKeyResolver;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.script.RedisScript;
import org.springframework.stereotype.Component;

import java.security.Key;
import java.util.List;


public class SlidingWindowRateLimiterService implements RateLimiterService{

    private final RedisTemplate<String, String> redisTemplate;
    private final RateLimitKeyResolver keyResolver;
    private final RedisScript<List> script;

    public SlidingWindowRateLimiterService(RedisTemplate<String, String> redisTemplate,
                                           RateLimitKeyResolver keyResolver,
                                           RedisScript<List> script) {
        this.redisTemplate = redisTemplate;
        this.keyResolver = keyResolver;
        this.script=script;
    }

    @Override
    public RateLimitResult isAllowed(RateLimitContext context, HttpServletRequest request) {

        String key = keyResolver.resolve(context,request);
        long now = System.currentTimeMillis();
        List<?> result = redisTemplate.execute(
                script,
                List.of(key),
                String.valueOf(context.getRateLimit().limit()),
                String.valueOf(context.getRateLimit().durationSeconds()),
                String.valueOf(now)
        );

        boolean allowed = ((Long) result.get(0))==1;
        long currentCount = (Long) result.get(1);
        long retryAfterSeconds = (Long) result.get(2);

        long remaining = Math.max(0, context.getRateLimit().limit() - currentCount);

        long resetEpochSeconds = retryAfterSeconds > 0
                        ? (now / 1000) + retryAfterSeconds
                        : (now / 1000) + context.getRateLimit().durationSeconds();

        return new RateLimitResult(
                allowed,
                remaining,
                resetEpochSeconds
        );
    }
}

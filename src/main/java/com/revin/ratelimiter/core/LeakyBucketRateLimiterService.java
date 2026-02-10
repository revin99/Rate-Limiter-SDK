package com.revin.ratelimiter.core;

import com.revin.ratelimiter.context.RateLimitContext;
import com.revin.ratelimiter.key.RateLimitKeyResolver;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.script.RedisScript;
import org.springframework.stereotype.Component;

import java.util.List;


public class LeakyBucketRateLimiterService implements RateLimiterService{


    private final RedisTemplate<String, String> redisTemplate;
    private final RateLimitKeyResolver keyResolver;
    private final RedisScript<List> script;

    public LeakyBucketRateLimiterService(RedisTemplate<String, String> redisTemplate,
                                         RateLimitKeyResolver keyResolver,
                                         RedisScript<List> script) {
        this.redisTemplate = redisTemplate;
        this.keyResolver = keyResolver;
        this.script = script;
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


        boolean allowed = ((Long) result.get(0)) == 1;
        long level = (Long) result.get(1);

        long remaining = Math.max(0, context.getRateLimit().limit() - level);
        long resetEpochSeconds =
                (now / 1000) + context.getRateLimit().durationSeconds();

        return new RateLimitResult(allowed, remaining, resetEpochSeconds);

    }
}

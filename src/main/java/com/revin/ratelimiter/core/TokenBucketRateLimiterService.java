package com.revin.ratelimiter.core;

import com.revin.ratelimiter.context.RateLimitContext;
import com.revin.ratelimiter.key.RateLimitKeyResolver;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.script.RedisScript;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Objects;
import java.util.concurrent.TimeUnit;


public class TokenBucketRateLimiterService implements RateLimiterService{

    private final RedisTemplate<String, String> redisTemplate;
    private final RateLimitKeyResolver keyResolver;
    private final RedisScript<Long> tokenBucketLuaScript;


    public TokenBucketRateLimiterService(RedisTemplate<String, String> redisTemplate,
                                         RateLimitKeyResolver keyResolver,
                                         RedisScript<Long> tokenBucketLuaScript) {
        this.redisTemplate = redisTemplate;
        this.keyResolver = keyResolver;
        this.tokenBucketLuaScript=tokenBucketLuaScript;
    }


    @Override
    public RateLimitResult isAllowed(RateLimitContext context, HttpServletRequest request) {

        String key = keyResolver.resolve(context,request);

        long nowMillis = System.currentTimeMillis();
        long nowEpochSeconds = nowMillis / 1000;


        Long result = redisTemplate.execute(
                tokenBucketLuaScript,
                List.of(key),
                String.valueOf(context.getRateLimit().limit()),
                String.valueOf(context.getRateLimit().durationSeconds()),
                String.valueOf(System.currentTimeMillis())
        );

        boolean allowed = result!=null && result>=0;

        long tokensLeft = allowed?result:0;
        long capacity = context.getRateLimit().limit();
        long refillSeconds = context.getRateLimit().durationSeconds();

        long secondsToFull = ((capacity-tokensLeft)*refillSeconds)/capacity;
        long resetEpochSeconds = nowEpochSeconds + Math.max(0,secondsToFull);

        return new RateLimitResult(
                allowed,
                tokensLeft,
                resetEpochSeconds
        );


    }
}

package com.revin.ratelimiter.core;

import com.revin.ratelimiter.context.RateLimitContext;
import com.revin.ratelimiter.key.RateLimitKeyResolver;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.util.Objects;
import java.util.concurrent.TimeUnit;


public class TokenBucketRateLimiterService implements RateLimiterService{

    private final RedisTemplate<String, String> redisTemplate;
    private final RateLimitKeyResolver keyResolver;


    public TokenBucketRateLimiterService(RedisTemplate<String, String> redisTemplate, RateLimitKeyResolver keyResolver) {
        this.redisTemplate = redisTemplate;
        this.keyResolver = keyResolver;
    }

    private void initializeBucket(String key, int capacity, int refillSeconds) {
        redisTemplate.opsForValue().set(
                key,
                String.valueOf(capacity - 1),
                refillSeconds, //after this many seconds, the key is deleted and bucket becomes null
                TimeUnit.SECONDS
        );
    }

    private void consumeToken(String key) {
        redisTemplate.opsForValue().decrement(key);
    }

    @Override
    public boolean isAllowed(RateLimitContext context, HttpServletRequest request) {

        String baseKey = keyResolver.resolve(context,request);
        String tokensKey = baseKey +":tokens";
        String tsKey = baseKey + ":lastRefill";


        long capacity = context.getRateLimit().limit();
        long refillSeconds = context.getRateLimit().durationSeconds();
        long now = System.currentTimeMillis();

        String tokenStr = redisTemplate.opsForValue().get(tokensKey);
        String lastRefillStr = redisTemplate.opsForValue().get(tsKey);

        long tokens;
        long lastRefillTime;

        // First hit (initialize bucket)
        if (tokenStr == null || lastRefillStr == null) {
            tokens = capacity - 1;
            lastRefillTime = now;
        } else {
            tokens = Long.parseLong(tokenStr);
            lastRefillTime = Long.parseLong(lastRefillStr);

            // Refill logic
            long elapsedMillis = now - lastRefillTime;
            long refillTokens = (elapsedMillis * capacity) / (refillSeconds * 1000L);

            if (refillTokens > 0) {
                tokens = Math.min(capacity, tokens + refillTokens);
                lastRefillTime = now;
            }

            // No tokens available
            if (tokens <= 0) {
                return false;
            }

            // Consume 1 token
            tokens--;
        }
        long ttlSeconds = refillSeconds*2;
        // Persist state
        redisTemplate.opsForValue().set(tokensKey, String.valueOf(tokens), ttlSeconds, TimeUnit.SECONDS);
        redisTemplate.opsForValue().set(tsKey, String.valueOf(lastRefillTime), ttlSeconds, TimeUnit.SECONDS);

        return true;

    }
}

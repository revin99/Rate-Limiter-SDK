package com.revin.ratelimiter.core;

import com.revin.ratelimiter.context.RateLimitContext;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.time.Duration;

@Component
public class FixedWindowRateLimiterService implements RateLimiterService {

    private final RedisTemplate<String,String> redisTemplate;

    public FixedWindowRateLimiterService(@Qualifier("redisTemplate")RedisTemplate<String, String> redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    @Override
    public boolean isAllowed(RateLimitContext context, HttpServletRequest request) {

        String key = "ratelimiter:count:"+request.getRequestURI();
        Long count = redisTemplate.opsForValue().increment(key);

        if(count==1){
            redisTemplate.expire(key, Duration.ofSeconds(context.getRateLimit().durationSeconds()));
        }

        return count <=context.getRateLimit().limit();

    }
}

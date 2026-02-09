package com.revin.ratelimiter.core;

import com.revin.ratelimiter.context.RateLimitContext;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.stereotype.Component;

@Component
public class SlidingWindowRateLimiterService implements RateLimiterService{
    @Override
    public boolean isAllowed(RateLimitContext context, HttpServletRequest request) {
        return true;
    }
}

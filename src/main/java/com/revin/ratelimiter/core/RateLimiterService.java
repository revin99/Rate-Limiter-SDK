package com.revin.ratelimiter.core;

import com.revin.ratelimiter.context.RateLimitContext;
import jakarta.servlet.http.HttpServletRequest;

public interface RateLimiterService {

    RateLimitResult isAllowed(RateLimitContext context, HttpServletRequest request);
}

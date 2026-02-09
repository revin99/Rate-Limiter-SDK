package com.revin.ratelimiter.key;

import com.revin.ratelimiter.context.RateLimitContext;
import jakarta.servlet.http.HttpServletRequest;

public interface RateLimitKeyResolver {

    String resolve(RateLimitContext context, HttpServletRequest request);
}

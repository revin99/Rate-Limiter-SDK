package com.revin.ratelimiter.context;

import com.revin.ratelimiter.annotation.RateLimit;
import org.springframework.web.method.HandlerMethod;

public class RateLimitContext {

    private final HandlerMethod handlerMethod;
    private final RateLimit rateLimit;

    public RateLimitContext(HandlerMethod handlerMethod, RateLimit rateLimit) {
        this.handlerMethod = handlerMethod;
        this.rateLimit = rateLimit;
    }

    public HandlerMethod getHandlerMethod() {
        return handlerMethod;
    }

    public RateLimit getRateLimit() {
        return rateLimit;
    }
}

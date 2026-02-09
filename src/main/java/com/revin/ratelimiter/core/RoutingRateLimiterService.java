package com.revin.ratelimiter.core;

import com.revin.ratelimiter.context.RateLimitContext;
import jakarta.servlet.http.HttpServletRequest;

import java.util.Map;

public class RoutingRateLimiterService implements RateLimiterService {

    private final Map<Algo, RateLimiterService> rateLimiters;

    public RoutingRateLimiterService(Map<Algo, RateLimiterService> rateLimiters) {
        this.rateLimiters = rateLimiters;
    }

    @Override
    public boolean isAllowed(RateLimitContext context, HttpServletRequest request) {

        Algo algo = context.getRateLimit().algorithm();
        RateLimiterService delegate = rateLimiters.get(algo);

        if(delegate==null){
            throw  new IllegalStateException(
                    "No RateLimiterService found for algorithm: " + algo
            );
        }

        return delegate.isAllowed(context,request);

    }
}

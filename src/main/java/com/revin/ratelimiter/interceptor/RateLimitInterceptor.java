package com.revin.ratelimiter.interceptor;


import com.revin.ratelimiter.annotation.RateLimit;
import com.revin.ratelimiter.context.RateLimitContext;
import com.revin.ratelimiter.core.RateLimitResult;
import com.revin.ratelimiter.core.RateLimiterService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.LoggerFactory;
import org.slf4j.Logger;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;

import java.io.IOException;
import java.time.Duration;


public class RateLimitInterceptor implements HandlerInterceptor {

    private static final Logger log = LoggerFactory.getLogger(RateLimitInterceptor.class);

    private final RateLimiterService rateLimiterService;

    public RateLimitInterceptor(RateLimiterService rateLimiterService){
        this.rateLimiterService = rateLimiterService;

    }

    @Override
    public boolean preHandle(
            HttpServletRequest request,
            HttpServletResponse response,
            Object handler
    ) throws IOException {
        if(!(handler instanceof HandlerMethod handlerMethod)){
            return true;
        }

        RateLimit rateLimit = handlerMethod.getMethodAnnotation(RateLimit.class);

        if(rateLimit==null){
            return true;
        }

        RateLimitContext context = new RateLimitContext(handlerMethod,rateLimit);

        RateLimitResult result = rateLimiterService.isAllowed(context,request);

        response.setHeader(
                "X-RateLimit-Limit",
                String.valueOf(context.getRateLimit().limit())
        );
        response.setHeader(
                "X-RateLimit-Remaining",
                String.valueOf(result.getRemaining())
        );
        response.setHeader(
                "X-RateLimit-Reset",
                String.valueOf(result.getResetEpochSeconds())
        );

        if(!result.isAllowed()){
            long nowEpochSeconds = System.currentTimeMillis()/1000;
            long retryAfter = Math.max(0,result.getResetEpochSeconds()-nowEpochSeconds);
            response.setHeader("Retry-After",String.valueOf(retryAfter));
            response.setStatus(429);
            return false;
        }

        log.info("""
                RateLimit detected
                Controller: {}
                Method: {}
                Limit: {}
                Duration: {}
                Algorithm: {}
                """,
                handlerMethod.getBeanType().getSimpleName(),
                handlerMethod.getMethod().getName(),
                rateLimit.limit(),
                rateLimit.durationSeconds(),
                rateLimit.algorithm());

        return true;
    }
}

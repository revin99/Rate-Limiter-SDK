package com.revin.ratelimiter.key;

import com.revin.ratelimiter.context.RateLimitContext;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.stereotype.Component;


public class DefaultRateLimitKeyResolver implements RateLimitKeyResolver{


    @Override
    public String resolve(RateLimitContext context, HttpServletRequest request) {

        String clientIp = request.getRemoteAddr();
        String handler = context.getHandlerMethod().getBeanType().getSimpleName()
                +"#"+context.getHandlerMethod().getMethod().getName();

        return "rate_limit:"+clientIp+":"+handler;
    }
}

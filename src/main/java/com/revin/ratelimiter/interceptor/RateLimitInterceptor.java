package com.revin.ratelimiter.interceptor;


import com.revin.ratelimiter.annotation.RateLimit;
import com.revin.ratelimiter.context.RateLimitContext;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.LoggerFactory;
import org.slf4j.Logger;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;



public class RateLimitInterceptor implements HandlerInterceptor {

    private static final Logger log = LoggerFactory.getLogger(RateLimitInterceptor.class);

    @Override
    public boolean preHandle(
            HttpServletRequest request,
            HttpServletResponse response,
            Object handler
    ){
        if(!(handler instanceof HandlerMethod handlerMethod)){
            return true;
        }

        RateLimit rateLimit = handlerMethod.getMethodAnnotation(RateLimit.class);

        if(rateLimit==null){
            return true;
        }

        RateLimitContext context = new RateLimitContext(handlerMethod,rateLimit);

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

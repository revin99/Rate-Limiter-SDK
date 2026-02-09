package com.revin.ratelimiter.interceptor;


import com.revin.ratelimiter.annotation.RateLimit;
import com.revin.ratelimiter.context.RateLimitContext;
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

    private final RedisTemplate<String,String> redisTemplate;
    public RateLimitInterceptor(RedisTemplate<String, String> redisTemplate){

        this.redisTemplate = redisTemplate;
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


        String key = "ratelimiter:hit:" + request.getRequestURI();
        Long count = redisTemplate.opsForValue().increment(key);

        int limit = rateLimit.limit();
        if(count>limit){
            response.setStatus(429);
            response.getWriter().write("Too many requests");
            return false;
        }

        if(count==1) {
            redisTemplate.expire(key, Duration.ofSeconds(rateLimit.durationSeconds()));
        }

        log.info("Stored in Redis -> {} = {}", key, count);

        return true;
    }
}

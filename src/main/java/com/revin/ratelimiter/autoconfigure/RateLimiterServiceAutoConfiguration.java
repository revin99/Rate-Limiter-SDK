package com.revin.ratelimiter.autoconfigure;

import com.revin.ratelimiter.core.*;
import com.revin.ratelimiter.key.DefaultRateLimitKeyResolver;
import com.revin.ratelimiter.key.RateLimitKeyResolver;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.data.redis.core.RedisTemplate;

import java.util.EnumMap;
import java.util.Map;

@Configuration
public class RateLimiterServiceAutoConfiguration {



    @Bean
    public Map<Algo,RateLimiterService> rateLimiterMap(
            FixedWindowRateLimiterService fixedWindow,
            TokenBucketRateLimiterService tokenBucket,
            LeakyBucketRateLimiterService leakyBucket,
            SlidingWindowRateLimiterService slidingWindow
    ){
        Map<Algo,RateLimiterService> map = new EnumMap<>(Algo.class);
        map.put(Algo.FIXED_WINDOW,fixedWindow);
        map.put(Algo.LEAKY_BUCKET,leakyBucket);
        map.put(Algo.TOKEN_BUCKET,tokenBucket);
        map.put(Algo.SLIDING_WINDOW,slidingWindow);

        return map;
    }

    @Bean
    @ConditionalOnMissingBean
    public RateLimitKeyResolver rateLimitKeyResolver() {
        return new DefaultRateLimitKeyResolver();
    }

    @Bean
    public FixedWindowRateLimiterService fixedWindowRateLimiterService(
            @Qualifier("redisTemplate") RedisTemplate<String, String> redisTemplate,
            RateLimitKeyResolver keyResolver
    ) {
        return new FixedWindowRateLimiterService(redisTemplate, keyResolver);
    }

    @Bean
    public TokenBucketRateLimiterService tokenBucketRateLimiterService(
            @Qualifier("redisTemplate")
            RedisTemplate<String, String> redisTemplate
    ) {
        return new TokenBucketRateLimiterService();
    }

    @Bean
    public LeakyBucketRateLimiterService leakyBucketRateLimiterService(
            @Qualifier("redisTemplate")
            RedisTemplate<String, String> redisTemplate
    ) {
        return new LeakyBucketRateLimiterService();
    }

    @Bean
    public SlidingWindowRateLimiterService slidingWindowRateLimiterService(
            @Qualifier("redisTemplate")
            RedisTemplate<String, String> redisTemplate
    ) {
        return new SlidingWindowRateLimiterService();
    }


    @Bean
    @Primary
    public RateLimiterService rateLimiterService( Map<Algo, RateLimiterService> rateLimiterMap) {
        return new RoutingRateLimiterService(rateLimiterMap);
    }
}

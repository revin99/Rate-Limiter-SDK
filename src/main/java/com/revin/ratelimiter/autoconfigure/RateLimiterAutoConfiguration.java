package com.revin.ratelimiter.autoconfigure;

import jakarta.annotation.PostConstruct;
import org.slf4j.LoggerFactory;
import org.slf4j.Logger;
import org.springframework.context.annotation.Configuration;


@Configuration
public class RateLimiterAutoConfiguration {

    private static final Logger log = LoggerFactory.getLogger(RateLimiterAutoConfiguration.class);

    @PostConstruct
    public void init(){
        log.info("Ratelimiter initalised");
    }
}

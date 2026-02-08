package com.revin.ratelimiter.annotation;


import com.revin.ratelimiter.core.Algo;

import java.lang.annotation.*;

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface RateLimit {

    int limit();

    int durationSeconds();

    Algo algorithm() default Algo.TOKEN_BUCKET;
}

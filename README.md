# 🚦 Distributed Rate Limiting SDK (Spring Boot + Redis)

A **pluggable, distributed rate-limiting SDK** built using **Spring Boot and Redis**, supporting **four industry-standard rate-limiting algorithms** with **Lua-based atomic execution**.

Designed as a **drop-in Spring Boot starter**, this SDK enables applications to enforce **consistent rate limits across multiple instances** in a distributed environment.

---

## ✨ Features

- Distributed rate limiting using Redis
- Pluggable architecture (algorithm can be switched without code changes)
- Spring Boot auto-configuration
- Four industry-standard rate-limiting algorithms
- Redis Lua scripts for atomic execution
- Standard rate-limit response headers
- Automatic Redis key cleanup via TTL
- Production-ready design

---

## 🧠 Supported Algorithms

| Algorithm | Description |
|---------|-------------|
| Fixed Window | Limits requests in fixed time windows |
| Sliding Window | Smooth rate limiting using timestamps |
| Token Bucket | Allows bursts with gradual refill |
| Leaky Bucket | Enforces a constant processing rate |

---

## 🏗️ Architecture Overview

```text
Consumer Application
   |
   |-- RateLimitInterceptor
         |
         |-- RoutingRateLimiterService
                |
                |-- FixedWindowRateLimiterService
                |-- SlidingWindowRateLimiterService
                |-- TokenBucketRateLimiterService
                |-- LeakyBucketRateLimiterService
                         |
                         |-- Redis + Lua Scripts

```
## 📦 Installation

```text
<dependency>
    <groupId>com.revin</groupId>
    <artifactId>spring-boot-starter-rate-limiter</artifactId>
    <version>1.0.0</version>
</dependency>
```

## 🧰 Running Redis
```text
version: "3.8"
services:
  redis:
    image: redis:7
    ports:
      - "6379:6379"
```

## ⚙️ Configuration
### Redis Configuration (Consumer App)
```text
spring:
  redis:
    host: localhost
    port: 6379
```

## 🚀 Usage
### Annotate APIs
```text
@RateLimit(
    limit = 5,
    durationSeconds = 10,
    algorithm = Algo.SLIDING_WINDOW
)
@GetMapping("/hello")
public String hello() {
    return "Hello World";
}
```

## 🔁 Supported Algorithms
```text
FIXED_WINDOW
SLIDING_WINDOW
TOKEN_BUCKET
LEAKY_BUCKET
```

## 🔑 Rate Limit Key Strategy
Default Key Format:
```text
rate_limit:<client-ip>:<controller-method>
```
Example:
```text
rate_limit:127.0.0.1:TestController#hello
```

## 📊 Rate Limit Response Headers

| Header | Description |
|---------|-------------|
| X-RateLimit-Limit | Max allowed requests |
| X-RateLimit-Remaining | Remaining requests |
| X-RateLimit-Reset | Epoch time when window resets |
| Retry-After | Seconds to wait before retry |

### Example 429 Too Many Requests
```text
HTTP/1.1 429 Too Many Requests
X-RateLimit-Limit: 5
X-RateLimit-Remaining: 0
X-RateLimit-Reset: 1707582330
Retry-After: 3
```

## 🧹 Cleanup Strategy

- Redis keys expire automatically using TTL
- No background cleanup jobs required
- Prevents Redis memory leaks

## 🛠 Extensibility

To add a new algorithm:
1. Implement RateLimiterService
2. Add a Lua script (if needed)
3. Register the service as a Spring bean
4. Add enum entry

## 📌 Future Enhancements

- Metrics via Micrometer / Prometheus
- Redis Cluster support
- API key–based quotas
- Per-user or per-plan rate limits
- Adaptive rate limiting

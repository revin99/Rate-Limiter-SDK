package com.revin.ratelimiter.core;

public class RateLimitResult {

    private final boolean allowed;
    private final long remaining;
    private final long resetEpochSeconds;

    public RateLimitResult(boolean allowed, long remaining, long resetEpochSeconds) {
        this.allowed = allowed;
        this.remaining = remaining;
        this.resetEpochSeconds = resetEpochSeconds;
    }

    public boolean isAllowed() {
        return allowed;
    }

    public long getRemaining() {
        return remaining;
    }

    public long getResetEpochSeconds() {
        return resetEpochSeconds;
    }
}

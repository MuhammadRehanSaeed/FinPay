package com.rehancode.finpay.RateLimiter;

import lombok.Data;

import java.time.Duration;

@Data
public class RateLimitRule {
    private Long token;
    private Long refillTokens;
    private Duration refillTime;
    private RateLimitIdentity identity;

}

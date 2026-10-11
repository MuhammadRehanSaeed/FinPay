package com.rehancode.finpay.Common;

import com.rehancode.finpay.Model.RateLimitConfigModel;
import com.rehancode.finpay.RateLimiter.RateLimitRule;

public interface CacheService {
    RateLimitRule getCache(String key);

    void put(String key, RateLimitRule rateLimitRule);

    void remove(String key);

    void clear();
}

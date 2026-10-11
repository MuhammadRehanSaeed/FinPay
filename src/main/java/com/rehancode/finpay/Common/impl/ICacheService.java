package com.rehancode.finpay.Common.impl;

import com.rehancode.finpay.Common.CacheService;
import com.rehancode.finpay.Model.RateLimitConfigModel;
import com.rehancode.finpay.RateLimiter.RateLimitRule;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class ICacheService implements CacheService {
    Map<String, RateLimitRule> cache = new ConcurrentHashMap<>();


    public RateLimitRule getCache(String key) {
        return cache.get(key);
    }

    public void put(String key, RateLimitRule rateLimitRule) {
        cache.put(key, rateLimitRule);
    }

    public void remove(String key) {
        cache.remove(key);
    }

    public void clear() {
        cache.clear();
    }
}

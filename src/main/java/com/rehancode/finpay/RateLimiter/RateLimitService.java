package com.rehancode.finpay.RateLimiter;


import io.github.bucket4j.BandwidthBuilder;
import io.github.bucket4j.Bucket;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;


@Service
public class RateLimitService {

    Map<String, Bucket> bucketMap = new ConcurrentHashMap<>();

    public Bucket createBucket(RateLimitRule rule) {
        return Bucket.builder()
                .addLimit(
                        BandwidthBuilder
                                .builder()
                                .capacity(rule.getToken())
                                .refillGreedy(rule.getRefillTokens(), rule.getRefillTime()).build()
                ).build();
    }

    boolean isAllowed(String key, RateLimitRule rule) {
        Bucket bucket = bucketMap.computeIfAbsent(key, k -> createBucket(rule));

        return bucket.tryConsume(1);
    }
}

package com.rehancode.finpay.RateLimiter;


import com.rehancode.finpay.Common.CacheService;
import com.rehancode.finpay.Model.RateLimitConfigModel;
import com.rehancode.finpay.Repository.RateLimitConfigRepository;
import org.springframework.stereotype.Component;
import org.springframework.util.AntPathMatcher;

import java.time.Duration;
import java.util.List;
import java.util.Optional;

@Component
public class RateLimitConfig {
    private final RateLimitConfigRepository rateLimitConfigRepository;
    private final CacheService cacheService;
    private final AntPathMatcher pathMatcher = new AntPathMatcher();

    public RateLimitConfig(RateLimitConfigRepository rateLimitConfigRepository, CacheService cacheService) {
        this.rateLimitConfigRepository = rateLimitConfigRepository;
        this.cacheService = cacheService;
    }

//
//    public RateLimitRule getRule(String apiPath) {
//        RateLimitRule model=cacheService.getCache(apiPath);
//        if(model!=null){
//            return model;
//        }
//        model= rateLimitConfigRepository.findByApiPath(apiPath)
//                .map(this::mapToRule)
//                .orElseGet(this::getDefaultRule);
//        cacheService.put(apiPath, model);
//        return model;
//    }

    public RateLimitRule getRule(String requestPath) {
        // 1. Check cache first using the exact request path
        RateLimitRule cachedRule = cacheService.getCache(requestPath);
        if (cachedRule != null) {
            return cachedRule;
        }

        // 2. Try exact match from DB
        Optional<RateLimitConfigModel> configModel = rateLimitConfigRepository.findByApiPath(requestPath);

        // 3. If exact match fails, check against wildcard patterns stored in DB (e.g., /api/v1/transaction/*)
        if (configModel.isEmpty()) {
            List<RateLimitConfigModel> allConfigs = rateLimitConfigRepository.findAll();
            configModel = allConfigs.stream()
                    .filter(config -> pathMatcher.match(config.getApiPath(), requestPath))
                    .findFirst();
        }

        // 4. Map to rule or fallback to default, then cache it for future requests
        RateLimitRule rule = configModel.map(this::mapToRule).orElseGet(this::getDefaultRule);

        // Cache using the raw requestPath so next time it hits memory instantly
        cacheService.put(requestPath, rule);
        return rule;
    }

    private RateLimitRule mapToRule(RateLimitConfigModel model) {
        RateLimitRule rule = new RateLimitRule();
        rule.setToken(model.getCapacity());
        rule.setRefillTokens(model.getRefillTokens());
        // Convert stored seconds into a Java Duration
        rule.setRefillTime(Duration.ofSeconds(model.getRefillDurationSeconds()));
        rule.setIdentity(model.getIdentity());
        return rule;
    }

    private RateLimitRule getDefaultRule() {
        // Fallback rule for any unconfigured endpoint (e.g., 20 requests per minute by IP)
        RateLimitRule rule = new RateLimitRule();
        rule.setToken(20L);
        rule.setRefillTokens(5L);
        rule.setRefillTime(Duration.ofSeconds(60));
        rule.setIdentity(RateLimitIdentity.IP);
        return rule;
    }
}

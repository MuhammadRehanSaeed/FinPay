package com.rehancode.finpay.Model;

import com.rehancode.finpay.RateLimiter.RateLimitIdentity;
import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;

@Entity
@Table(name = "rate_limit_config")
@Data
public class RateLimitConfigModel {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "api_path", nullable = false, unique = true)
    private String apiPath;

    @Column(name = "capacity", nullable = false)
    private Long capacity; // Max tokens allowed in the bucket

    @Column(name = "refill_tokens", nullable = false)
    private Long refillTokens; // Tokens added per refill interval

    @Column(name = "refill_duration_seconds", nullable = false)
    private Long refillDurationSeconds; // Time duration in seconds

    @Enumerated(EnumType.STRING)
    @Column(name = "identity", nullable = false)
    private RateLimitIdentity identity; // IP or USERID

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
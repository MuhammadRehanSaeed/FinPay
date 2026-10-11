package com.rehancode.finpay.Repository;

import com.rehancode.finpay.Model.RateLimitConfigModel;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface RateLimitConfigRepository extends JpaRepository<RateLimitConfigModel, Long> {

    // Useful for fetching rate limit configuration dynamically when a request comes in
    Optional<RateLimitConfigModel> findByApiPath(String apiPath);
}
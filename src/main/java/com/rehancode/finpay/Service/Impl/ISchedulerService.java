package com.rehancode.finpay.Service.Impl;

import com.rehancode.finpay.Repository.IdempotencyRecordRepository;
import com.rehancode.finpay.Service.SchedulerService;
import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;


@Service
@Slf4j
public class ISchedulerService implements SchedulerService {
    //private final Logger log = LoggerFactory.getLogger(this.getClass());
    private final IdempotencyRecordRepository repository;

    public ISchedulerService(IdempotencyRecordRepository repository) {
        this.repository = repository;
    }

    @Scheduled(cron = "0 0 2 * * *")
    @Transactional
    public void deleteIdempotencyRecord() {
        long startTime = System.nanoTime();

        LocalDateTime cutOff = LocalDateTime.now().minusDays(30);

        log.info("Idempotency cleanup started. cutoff={}", cutOff);

        try {
            int deleted = repository.deleteBeforeCreatedDate(cutOff);

            long durationMs = (System.nanoTime() - startTime) / 1_000_000;

            log.info(
                    "Idempotency cleanup completed. deletedRecords={}, durationMs={}",
                    deleted,
                    durationMs
            );
        } catch (RuntimeException e) {
            log.error("Idempotency cleanup failed. cutoff={}", cutOff, e);
            throw e;
        }
    }
}

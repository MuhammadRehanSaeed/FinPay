package com.rehancode.finpay.Service.Impl;

import com.rehancode.finpay.DTO.AuditLogDTO;
import com.rehancode.finpay.Enum.TransactionStatus;
import com.rehancode.finpay.Enum.TransactionType;
import com.rehancode.finpay.Model.AuditLogModel;
import com.rehancode.finpay.Repository.AuditLogRepository;
import com.rehancode.finpay.Service.AuditService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.concurrent.CompletableFuture;

@Slf4j
@Service
public class IAuditService implements AuditService {
    private final AuditLogRepository auditLogRepository;

    public IAuditService(AuditLogRepository auditLogRepository) {
        this.auditLogRepository = auditLogRepository;
    }


    @Async("auditExecutor")
    @Override
    public CompletableFuture<Void> auditLog(AuditLogDTO dto) {
        AuditLogModel auditLogModel = new AuditLogModel();
        auditLogModel.setUserId(dto.getUserId());
        auditLogModel.setAction(dto.getAction());
        auditLogModel.setEntityId(dto.getEntityId());
        auditLogModel.setEntityType(dto.getEntityType());
        auditLogModel.setStatus(dto.getStatus());
        auditLogModel.setDetails(dto.getDetails());
        auditLogModel.setIpAddress(dto.getIpAddress());
        auditLogModel.setUserAgent(dto.getUserAgent());
        auditLogRepository.save(auditLogModel);
        log.info("audit for transfer completed");
        return CompletableFuture.completedFuture(null);
    }
}

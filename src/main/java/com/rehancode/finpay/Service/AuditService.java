package com.rehancode.finpay.Service;

import com.rehancode.finpay.DTO.AuditLogDTO;
import com.rehancode.finpay.Model.AuditLogModel;
import jakarta.validation.Valid;

import java.util.concurrent.CompletableFuture;

public interface AuditService {
    CompletableFuture<Void> auditLog(@Valid AuditLogDTO auditLogDTO);
}

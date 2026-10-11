package com.rehancode.finpay.DTO;

import lombok.Data;

@Data
public class AuditLogDTO {
    private Long userId;
    private String action;
    private Long entityId;
    private String entityType;
    private String status;
    private String details;
    private String ipAddress;
    private String userAgent;
}

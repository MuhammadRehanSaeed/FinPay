package com.rehancode.finpay.Repository;

import com.rehancode.finpay.Model.AuditLogModel;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AuditLogRepository extends JpaRepository<AuditLogModel, Long> {

    // Useful for finding a specific user's audit trail with pagination
    Page<AuditLogModel> findByUserId(Long userId, Pageable pageable);

    // Useful for filtering audit logs by action type (e.g., all FAILED logins or DEPOSITS)
    Page<AuditLogModel> findByAction(String action, Pageable pageable);
}
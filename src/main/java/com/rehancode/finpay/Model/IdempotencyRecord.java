package com.rehancode.finpay.Model;


import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;


@Entity
@Data
@Table(name = "IdempotencyRecord")
public class IdempotencyRecord {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ID")
    private Long Id;

    @Column(name = "IdempotencyKey", nullable = false, unique = true)
    private String idempotencyKey;

    @Column(name = "Status")
    private String status;

    @OneToOne
    @JoinColumn(name = "transaction_id")
    private TransactionModel transactionModel;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;
}

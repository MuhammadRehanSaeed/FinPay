package com.rehancode.finpay.Model;

import com.rehancode.finpay.Enum.LedgerType;
import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.CreationTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "Ledger")
@Data
public class LedgerModel {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ID")
    private Long Id;

    @ManyToOne
    @JoinColumn(name = "transaction_id", nullable = false)
    private TransactionModel transaction;

    @ManyToOne
    @JoinColumn(name = "wallet_id", nullable = false)
    private WalletModel wallet;
    @Enumerated(EnumType.STRING)
    private LedgerType ledgerType;

    private BigDecimal amount;

    private BigDecimal beforeAmount;

    private BigDecimal afterAmount;

    @CreationTimestamp
    private LocalDateTime createdAt;

}

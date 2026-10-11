package com.rehancode.finpay.Model;


import com.rehancode.finpay.Enum.TransactionStatus;
import com.rehancode.finpay.Enum.TransactionType;
import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.CreationTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "Transactions")
@Data
public class TransactionModel {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String reference;

    @ManyToOne
    @JoinColumn(name = "sender_wallet_id")
    private WalletModel senderWallet;

    @ManyToOne
    @JoinColumn(name = "receiver_wallet_id")
    private WalletModel receiverWallet;

    @Column(nullable = false)
    private BigDecimal amount;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TransactionType type;

//    @Column(name = "IdempotencyKey",nullable = false,unique = true)
//    private String idempotencyKey;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TransactionStatus status;

    @CreationTimestamp
    private LocalDateTime createdAt;
}

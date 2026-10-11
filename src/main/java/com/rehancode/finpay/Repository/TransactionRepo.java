package com.rehancode.finpay.Repository;

import com.rehancode.finpay.Enum.Status;
import com.rehancode.finpay.Model.TransactionModel;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;

@Repository
public interface TransactionRepo extends JpaRepository<TransactionModel, Long> {


    // boolean existsByIdempotencyKey(String idempotencyKey);

    List<TransactionModel> findBySenderWalletId(Long userId);

    TransactionModel findBysenderWalletId(Long id);

    @Query("""
                SELECT t
                FROM TransactionModel t
                WHERE t.senderWallet.id = :walletId
                   OR t.receiverWallet.id = :walletId
                ORDER BY t.createdAt DESC
            """)
    List<TransactionModel> findUserTransactions(@Param("walletId") Long walletId);


    @Modifying
    @Query("update WalletModel w set w.balance= w.balance- :amount where w.id=:walletID AND w.balance>=:amount AND w.status=:status")
    int balanceSend(@Param("amount") BigDecimal amount, @Param("walletID") Long walletID, @Param("status") Status status);


    @Modifying
    @Query("update WalletModel w set w.balance= w.balance+ :amount where w.id=:walletID AND w.status=:status")
    int balanceReceive(@Param("amount") BigDecimal amount, @Param("walletID") Long walletID, @Param("status") Status status);


    @Query("SELECT w.balance FROM WalletModel w WHERE w.id = :walletID")
    BigDecimal findBalanceByWalletId(@Param("walletID") Long walletID);


}

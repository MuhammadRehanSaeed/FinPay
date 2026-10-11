package com.rehancode.finpay.Repository;

import com.rehancode.finpay.Enum.Status;
import com.rehancode.finpay.Model.WalletModel;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;

@Repository
public interface WalletRepository extends JpaRepository<WalletModel, Long> {

    boolean existsByUserId(Long userId);


    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select w from WalletModel w where w.user.id=:userId")
    WalletModel findByUserId(@Param("userId") Long userId);

    WalletModel findByUser_Id(Long userId);


    @Modifying
    @Query("update WalletModel w set w.balance= w.balance- :amount where w.user.id=:userId AND w.balance>=:amount AND w.status=:status")
    int withdrawBalance(@Param("amount") BigDecimal amount, @Param("userId") Long userIdx, @Param("status") Status status);


    @Modifying
    @Query("update WalletModel w set w.balance= w.balance+ :amount where w.user.id=:userId AND w.status=:status")
    int depositBalance(@Param("amount") BigDecimal amount, @Param("userId") Long userId, @Param("status") Status status);

    @Query("SELECT w.balance FROM WalletModel w WHERE w.user.id = :userId")
    BigDecimal findBalanceByUserId(@Param("userId") Long userId);
}

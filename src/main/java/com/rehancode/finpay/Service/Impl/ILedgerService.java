package com.rehancode.finpay.Service.Impl;

import com.rehancode.finpay.Enum.LedgerType;
import com.rehancode.finpay.Model.LedgerModel;
import com.rehancode.finpay.Model.TransactionModel;
import com.rehancode.finpay.Model.WalletModel;
import com.rehancode.finpay.Repository.LedgerRepo;
import com.rehancode.finpay.Service.LedgerService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;



@Slf4j
@Service
public class ILedgerService implements LedgerService {
    private final LedgerRepo ledgerRepo;

    public ILedgerService(LedgerRepo ledgerRepo) {
        this.ledgerRepo = ledgerRepo;
    }

    @Override
    public void addToLedger(
            TransactionModel transaction,
            WalletModel senderWallet,
            WalletModel receiverWallet,
            BigDecimal senderOldBalance,
            BigDecimal receiverOldBalance) {

        log.info("Creating ledger entry for sender {}", senderWallet.getId());


        BigDecimal amount = transaction.getAmount();

        // Sender ledger entry
        LedgerModel senderLedger = new LedgerModel();

        senderLedger.setTransaction(transaction);
        senderLedger.setWallet(senderWallet);
        senderLedger.setLedgerType(LedgerType.DEBIT);
        senderLedger.setAmount(amount);
        senderLedger.setBeforeAmount(senderOldBalance);
        senderLedger.setAfterAmount(
                senderOldBalance.subtract(amount)
        );

        ledgerRepo.save(senderLedger);

        log.info("Created ledger entry for sender {} with amount {}", senderWallet.getId(),senderLedger.getAfterAmount());

        log.info("Creating ledger entry for receiver {}", receiverWallet.getId());

        // Receiver ledger entry
        LedgerModel receiverLedger = new LedgerModel();

        receiverLedger.setTransaction(transaction);
        receiverLedger.setWallet(receiverWallet);
        receiverLedger.setLedgerType(LedgerType.CREDIT);
        receiverLedger.setAmount(amount);
        receiverLedger.setBeforeAmount(receiverOldBalance);
        receiverLedger.setAfterAmount(
                receiverOldBalance.add(amount)
        );

        ledgerRepo.save(receiverLedger);
        log.info("Created ledger entry for receiver {} with amount {}", receiverWallet.getId(),receiverLedger.getAfterAmount());

    }
}

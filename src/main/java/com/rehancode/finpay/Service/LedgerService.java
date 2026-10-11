package com.rehancode.finpay.Service;

import com.rehancode.finpay.Model.TransactionModel;
import com.rehancode.finpay.Model.WalletModel;

import java.math.BigDecimal;

public interface LedgerService {

    void addToLedger(TransactionModel transactionModel, WalletModel senderWallet, WalletModel receiverWallet, BigDecimal receiverOldAmount,BigDecimal senderOldAmount);
}

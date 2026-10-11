package com.rehancode.finpay.DTO;


import com.rehancode.finpay.Enum.TransactionType;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class TransactionHistoryDTO {
    private String tID;
    private String senderWallet;
    private String receiverWallet;
    private BigDecimal amount;
    private String type;
    private String direction;
    private String status;
}

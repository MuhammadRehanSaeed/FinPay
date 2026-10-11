package com.rehancode.finpay.DTO;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class TransferResponseDTO {
    private Long senderWalletId;
    private Long receiverWalletId;
    private BigDecimal amount;
}

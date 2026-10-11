package com.rehancode.finpay.DTO;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class WalletDTO {
    private BigDecimal balance;
    private String currency;
    private String status;
}

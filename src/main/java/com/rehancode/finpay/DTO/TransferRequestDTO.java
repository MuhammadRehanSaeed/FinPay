package com.rehancode.finpay.DTO;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class TransferRequestDTO {
    @NotBlank(message = "Receiver Id cannot be empty")
    private String receiverUserId;

    @NotNull(message = "Amount cannot be null")
    @DecimalMin(value = "0.01", message = "Amount must be greater than 0")
    @Digits(integer = 6, fraction = 2, message = "Amount can have up to 6 digits and 2 decimal places")
    private BigDecimal amount;
}

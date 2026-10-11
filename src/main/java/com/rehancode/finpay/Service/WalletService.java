package com.rehancode.finpay.Service;

import com.rehancode.finpay.DTO.DepositDTO;
import com.rehancode.finpay.DTO.WalletDTO;
import com.rehancode.finpay.Exception.ApiResponse;
import jakarta.validation.Valid;

public interface WalletService {
    ApiResponse<WalletDTO> fetchWallet(String ip,String userAgent);

    ApiResponse<WalletDTO> createWallet(@Valid WalletDTO currency,String ip,String userAgent);

    ApiResponse<WalletDTO> deposit(@Valid DepositDTO currency,String ip,String userAgent);

    ApiResponse<WalletDTO> withdraw(@Valid DepositDTO currency,String ip,String userAgent);
}

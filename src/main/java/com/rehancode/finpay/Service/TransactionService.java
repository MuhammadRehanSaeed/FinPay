package com.rehancode.finpay.Service;

import com.rehancode.finpay.DTO.TransactionHistoryDTO;
import com.rehancode.finpay.DTO.TransferRequestDTO;
import com.rehancode.finpay.DTO.TransferResponseDTO;
import com.rehancode.finpay.Exception.ApiResponse;

import java.util.List;

public interface TransactionService {
    ApiResponse<TransferResponseDTO> transfer(TransferRequestDTO transferRequestDTO,String idempotencyKey,String ip,String userAgent);

    ApiResponse<List<TransactionHistoryDTO>> transferHistory();

    ApiResponse<TransactionHistoryDTO> transferHistoryById(Long id);
}

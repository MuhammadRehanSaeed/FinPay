package com.rehancode.finpay.Controller;


import com.rehancode.finpay.DTO.TransactionHistoryDTO;
import com.rehancode.finpay.DTO.TransferRequestDTO;
import com.rehancode.finpay.DTO.TransferResponseDTO;
import com.rehancode.finpay.Exception.ApiResponse;
import com.rehancode.finpay.Service.TransactionService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("api/v1/transaction")
public class TransactionController extends BaseController {
    private final TransactionService transactionService;

    public TransactionController(TransactionService transactionService) {
        this.transactionService = transactionService;
    }


    @PostMapping("/transfer")
    public ResponseEntity<ApiResponse<TransferResponseDTO>> transfer(@RequestHeader("Idempotency-Key") String idempotencyKey, @RequestBody TransferRequestDTO transferRequestDTO, HttpServletRequest request) {
        String ip = getIpAddress(request);
        String userAgent = getUserAgent(request);
        log.info("POST /transfer - ip: {}, idempotencyKey: {}", ip, idempotencyKey);
        ApiResponse<TransferResponseDTO> res = transactionService.transfer(transferRequestDTO, idempotencyKey, ip, userAgent);
        return ResponseEntity.status(HttpStatus.OK).body(res);
    }

    @PostMapping
    public ResponseEntity<ApiResponse<List<TransactionHistoryDTO>>> transferHistory() {
        log.info("POST /transaction - fetching history");
        ApiResponse<List<TransactionHistoryDTO>> res = transactionService.transferHistory();
        return ResponseEntity.status(HttpStatus.OK).body(res);
    }

    @PostMapping("/{id}")
    public ResponseEntity<ApiResponse<TransactionHistoryDTO>> transferHistory(@PathVariable Long id) {
        log.info("POST /transaction/{} - fetching by id", id);
        ApiResponse<TransactionHistoryDTO> res = transactionService.transferHistoryById(id);
        return ResponseEntity.status(HttpStatus.OK).body(res);
    }


}

package com.rehancode.finpay.Controller;

import com.rehancode.finpay.DTO.DepositDTO;
import com.rehancode.finpay.DTO.WalletDTO;
import com.rehancode.finpay.Exception.ApiResponse;
import com.rehancode.finpay.Service.WalletService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequestMapping("/api/v1")
public class WalletController extends BaseController {
    private final WalletService walletService;

    public WalletController(WalletService walletService) {
        this.walletService = walletService;
    }


    @PostMapping("/wallet")
    public ResponseEntity<ApiResponse<WalletDTO>> fetchWallet(HttpServletRequest request) {
        String ip = getIpAddress(request);
        String userAgent = getUserAgent(request);
        log.info("POST /wallet - ip: {}", ip);
        ApiResponse<WalletDTO> response = walletService.fetchWallet(ip, userAgent);
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    @PostMapping("/createWallet")
    public ResponseEntity<ApiResponse<WalletDTO>> createWallet(@Valid @RequestBody WalletDTO currency, HttpServletRequest request) {
        String ip = getIpAddress(request);
        String userAgent = getUserAgent(request);
        log.info("POST /createWallet - ip: {}", ip);
        ApiResponse<WalletDTO> response = walletService.createWallet(currency, ip, userAgent);
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    @PostMapping("/wallet/deposit")
    public ResponseEntity<ApiResponse<WalletDTO>> deposit(@Valid @RequestBody DepositDTO currency, HttpServletRequest request) {
        String ip = getIpAddress(request);
        String userAgent = getUserAgent(request);
        log.info("POST /wallet/deposit - ip: {}", ip);
        ApiResponse<WalletDTO> response = walletService.deposit(currency, ip, userAgent);
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    @PostMapping("/wallet/withdraw")
    public ResponseEntity<ApiResponse<WalletDTO>> withdraw(@Valid @RequestBody DepositDTO currency, HttpServletRequest request) {
        String ip = getIpAddress(request);
        String userAgent = getUserAgent(request);
        log.info("POST /wallet/withdraw - ip: {}", ip);
        ApiResponse<WalletDTO> response = walletService.withdraw(currency, ip, userAgent);
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

}

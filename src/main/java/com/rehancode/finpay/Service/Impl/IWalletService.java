package com.rehancode.finpay.Service.Impl;

import com.rehancode.finpay.DTO.AuditLogDTO;
import com.rehancode.finpay.DTO.DepositDTO;
import com.rehancode.finpay.DTO.WalletDTO;
import com.rehancode.finpay.Enum.*;
import com.rehancode.finpay.Exception.*;
import com.rehancode.finpay.Model.*;
import com.rehancode.finpay.Repository.LedgerRepo;
import com.rehancode.finpay.Repository.TransactionRepo;
import com.rehancode.finpay.Repository.UserRepository;
import com.rehancode.finpay.Repository.WalletRepository;
import com.rehancode.finpay.Service.AuditService;
import com.rehancode.finpay.Service.NotificationService;
import com.rehancode.finpay.Service.WalletService;
import com.rehancode.finpay.Util.SecurityUtil;
import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

@Slf4j
@Service
public class IWalletService implements WalletService {
    private final WalletRepository walletRepository;
    private final UserRepository userRepository;
    private final TransactionRepo transactionRepo;
    private final LedgerRepo ledgerRepo;
    private final NotificationService notificationService;
    private final AuditService auditService;

    public IWalletService(WalletRepository walletRepository, UserRepository userRepository, TransactionRepo transactionRepo, LedgerRepo ledgerRepo, NotificationService notificationService, AuditService auditService) {
        this.walletRepository = walletRepository;
        this.userRepository = userRepository;
        this.transactionRepo = transactionRepo;
        this.ledgerRepo = ledgerRepo;
        this.notificationService = notificationService;
        this.auditService = auditService;
    }

    @Override
    @Transactional()
    public ApiResponse<WalletDTO> fetchWallet(String ip, String userAgent) {
        log.info("fetchWallet called - ip: {}", ip);
        Long userId = SecurityUtil.getAuthenticatedUserId();
        log.info("Fetching wallet for userId: {}", userId);
        WalletModel walletModel = walletRepository.findByUserId(userId);

        if (walletModel == null) {
            log.warn("No wallet found for userId: {}", userId);
            return new ApiResponse<>(HttpStatus.NOT_FOUND.value(), "No wallet found", null);
        }

        log.info("Wallet id: {} found for userId: {}", walletModel.getId(), userId);
        WalletDTO walletDTO = new WalletDTO();
        walletDTO.setBalance(walletModel.getBalance());
        walletDTO.setCurrency(String.valueOf(walletModel.getCurrency()));
        walletDTO.setStatus(walletModel.getStatus().toString());

        return new ApiResponse<>(HttpStatus.OK.value(), "Your Wallet", walletDTO);
    }

    @Override
    public ApiResponse<WalletDTO> createWallet(WalletDTO currency, String ip, String userAgent) {
        log.info("createWallet called - ip: {}", ip);
        Long userId = SecurityUtil.getAuthenticatedUserId();
        log.info("Creating wallet for userId: {}", userId);
        UsersModel usersModel = userRepository.findById(userId).orElseThrow(() -> new UsernameNotFoundException("User not found"));

        if (walletRepository.existsByUserId(userId)) {
            log.warn("Wallet already exists for userId: {}", userId);
            return new ApiResponse<>(HttpStatus.CONFLICT.value(), "Wallet already exists", null);
        }
        WalletModel wallet = new WalletModel();
        wallet.setBalance(BigDecimal.ZERO);
        wallet.setStatus(Status.ACTIVE);
        wallet.setUser(usersModel);
        wallet.setCurrency(Currency.valueOf(currency.getCurrency().toUpperCase()));

        walletRepository.save(wallet);
        log.info("Wallet id: {} created for userId: {}", wallet.getId(), userId);
        AuditLogDTO auditLogDTO=new AuditLogDTO();
        auditLogDTO.setUserId(usersModel.getId());
        auditLogDTO.setAction(String.valueOf(TransactionType.WALLET_CREATED));
        auditLogDTO.setEntityId(wallet.getId());
        auditLogDTO.setEntityType("Wallet Created");
        auditLogDTO.setStatus(String.valueOf(TransactionStatus.WALLET_CREATED));
        auditLogDTO.setDetails("Wallet with id: " + wallet.getId() + " created");
        auditLogDTO.setIpAddress(ip);
        auditLogDTO.setUserAgent(userAgent);
        CompletableFuture<Void> auditLog = auditService.auditLog(auditLogDTO);
        WalletDTO walletDTO = new WalletDTO();
        walletDTO.setBalance(wallet.getBalance());
        walletDTO.setCurrency(String.valueOf(wallet.getCurrency()));
        walletDTO.setStatus(wallet.getStatus().toString());

        return new ApiResponse<>(HttpStatus.OK.value(), "Wallet Created", walletDTO);


    }

    @Override
    @Transactional
    public ApiResponse<WalletDTO> deposit(DepositDTO currency, String ip, String userAgent) {
        log.info("deposit called - amount: {}, ip: {}", currency.getAmount(), ip);
        Long userId = SecurityUtil.getAuthenticatedUserId();
        log.info("Deposit for userId: {}", userId);
        UsersModel user = userRepository.findById(userId).orElseThrow(() -> new UsernameNotFoundException("User not found"));
        WalletModel walletModel = walletRepository.findByUserId(userId);
        if (walletModel == null) {
            log.warn("No wallet found for userId: {}", userId);
            return new ApiResponse<>(HttpStatus.NOT_FOUND.value(), "No wallet found", null);
        }
        if (walletModel.getStatus().equals(Status.BLOCK) || walletModel.getStatus().equals(Status.SUSPENDED)) {
            log.warn("Deposit blocked - wallet id: {} status: {}", walletModel.getId(), walletModel.getStatus());
            throw new UserNotAllowed("Not allowed to deposit");
        }

        BigDecimal oldBalance = walletRepository.findBalanceByUserId(userId);
        int rows = walletRepository.depositBalance(currency.getAmount(), userId, Status.ACTIVE);
        if (rows == 0) {
            log.warn("Deposit denied for userId: {} - wallet not active or insufficient state", userId);
            throw new UserNotAllowed("Not allowed to deposit");
        }
        BigDecimal newBalance = walletRepository.findBalanceByUserId(userId);
        log.info("Deposit successful for userId: {} - newBalance: {}", userId, newBalance);
        TransactionModel transactionModel = new TransactionModel();
        transactionModel.setAmount(currency.getAmount());
        transactionModel.setReceiverWallet(walletModel);
        transactionModel.setSenderWallet(null);
        transactionModel.setType(TransactionType.DEPOSIT);
        transactionModel.setReference(UUID.randomUUID().toString());
        transactionModel.setStatus(TransactionStatus.COMPLETED);
        transactionRepo.save(transactionModel);
        AuditLedger(currency, transactionModel, walletModel, oldBalance, newBalance, LedgerType.DEPOSIT);
        AuditLogDTO auditLogDTO = new AuditLogDTO();
        auditLogDTO.setUserId(user.getId());
        auditLogDTO.setAction(String.valueOf(TransactionType.DEPOSIT));
        auditLogDTO.setEntityId(walletModel.getId());
        auditLogDTO.setEntityType("Wallet Deposited");
        auditLogDTO.setStatus(String.valueOf(TransactionStatus.COMPLETED));
        auditLogDTO.setDetails(currency.getAmount() + " deposited in your wallet");
        auditLogDTO.setIpAddress(ip);
        auditLogDTO.setUserAgent(userAgent);
        CompletableFuture<Void> auditLog = auditService.auditLog(auditLogDTO);
        CompletableFuture<Void> emailSend = notificationService.sendEmail(user.getEmail(), "Transaction Deposited", "You have deposited " + currency.getAmount() + " in your wallet ");
        CompletableFuture.allOf(emailSend, auditLog);
        WalletDTO walletDTO = new WalletDTO();
        walletDTO.setBalance(newBalance);
        walletDTO.setCurrency(String.valueOf(walletModel.getCurrency()));
        walletDTO.setStatus(walletModel.getStatus().toString());

        return new ApiResponse<>(HttpStatus.OK.value(), "Deposit Success", walletDTO);
    }

    @Override
    @Transactional
    public ApiResponse<WalletDTO> withdraw(DepositDTO currency, String ip, String userAgent) {
        log.info("withdraw called - amount: {}, ip: {}", currency.getAmount(), ip);
        Long userId = SecurityUtil.getAuthenticatedUserId();
        log.info("Withdraw for userId: {}", userId);
        WalletModel walletModel = walletRepository.findByUserId(userId);

        UsersModel user = userRepository.findById(userId).orElseThrow(() -> new UsernameNotFoundException("User not found"));
        if (walletModel == null) {
            log.warn("No wallet found for userId: {}", userId);
            return new ApiResponse<>(HttpStatus.NOT_FOUND.value(), "No wallet found", null);
        }
        if (walletModel.getStatus().equals(Status.BLOCK) || walletModel.getStatus().equals(Status.SUSPENDED)) {
            log.warn("Withdraw blocked - wallet id: {} status: {}", walletModel.getId(), walletModel.getStatus());
            throw new UserNotAllowed("Not allowed to withdraw");
        }
//        if(walletModel.getBalance().compareTo(currency.getAmount())<0){
//           throw new InsufficientBalanceException("Insufficient Balance");
//        }
        BigDecimal oldBalance = walletRepository.findBalanceByUserId(userId);
        int rows = walletRepository.withdrawBalance(currency.getAmount(), userId, Status.ACTIVE);
        if (rows == 0) {
            log.warn("Withdraw failed - insufficient balance for userId: {}", userId);
            throw new InsufficientBalanceException("Insufficient Balance");
        }
        BigDecimal newBalance = walletRepository.findBalanceByUserId(userId);
        log.info("Withdraw successful for userId: {} - newBalance: {}", userId, newBalance);
//        BigDecimal newBalance = walletModel.getBalance()
//                .subtract(currency.getAmount());
//        walletModel.setBalance(newBalance);
//        walletRepository.save(walletModel);
        TransactionModel transactionModel = new TransactionModel();
        transactionModel.setAmount(currency.getAmount());
        transactionModel.setReceiverWallet(null);
        transactionModel.setSenderWallet(walletModel);
        transactionModel.setType(TransactionType.WITHDRAW);
        transactionModel.setReference(UUID.randomUUID().toString());
        transactionModel.setStatus(TransactionStatus.COMPLETED);
        transactionRepo.save(transactionModel);
        AuditLedger(currency, transactionModel, walletModel, oldBalance, newBalance, LedgerType.WITHDRAW);
        AuditLogDTO auditLogDTO = new AuditLogDTO();
        auditLogDTO.setUserId(user.getId());
        auditLogDTO.setAction(String.valueOf(TransactionType.WITHDRAW));
        auditLogDTO.setEntityId(walletModel.getId());
        auditLogDTO.setEntityType("Wallet Withdrawn");
        auditLogDTO.setStatus(String.valueOf(TransactionStatus.COMPLETED));
        auditLogDTO.setDetails(currency.getAmount() + " withdrawn in your wallet");
        auditLogDTO.setIpAddress(ip);
        auditLogDTO.setUserAgent(userAgent);
        CompletableFuture<Void> emailSend = notificationService.sendEmail(user.getEmail(), "Transaction Withdrawn", "You have withdrew " + currency.getAmount() + " from your wallet ");
        CompletableFuture<Void> auditLog = auditService.auditLog(auditLogDTO);
        CompletableFuture.allOf(emailSend, auditLog);

        WalletDTO walletDTO = new WalletDTO();
        walletDTO.setBalance(newBalance);
        walletDTO.setCurrency(String.valueOf(walletModel.getCurrency()));
        walletDTO.setStatus(walletModel.getStatus().toString());

        return new ApiResponse<>(HttpStatus.OK.value(), "Withdraw Success", walletDTO);
    }

    private void AuditLedger(DepositDTO currency, TransactionModel transactionModel, WalletModel walletModel, BigDecimal oldBalance, BigDecimal newBalance, LedgerType ledgerType) {
        LedgerModel ledgerModel = new LedgerModel();
        ledgerModel.setTransaction(transactionModel);
        ledgerModel.setWallet(walletModel);
        ledgerModel.setLedgerType(ledgerType);
        ledgerModel.setBeforeAmount(oldBalance);
        ledgerModel.setAmount(currency.getAmount());
        ledgerModel.setAfterAmount(newBalance);
        ledgerRepo.save(ledgerModel);
    }
}

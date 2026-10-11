package com.rehancode.finpay.Service.Impl;

import com.rehancode.finpay.DTO.AuditLogDTO;
import com.rehancode.finpay.DTO.TransactionHistoryDTO;
import com.rehancode.finpay.DTO.TransferRequestDTO;
import com.rehancode.finpay.DTO.TransferResponseDTO;
import com.rehancode.finpay.Enum.Status;
import com.rehancode.finpay.Enum.TransactionStatus;
import com.rehancode.finpay.Enum.TransactionType;
import com.rehancode.finpay.Exception.*;
import com.rehancode.finpay.Model.IdempotencyRecord;
import com.rehancode.finpay.Model.TransactionModel;
import com.rehancode.finpay.Model.UsersModel;
import com.rehancode.finpay.Model.WalletModel;
import com.rehancode.finpay.Repository.IdempotencyRecordRepository;
import com.rehancode.finpay.Repository.TransactionRepo;
import com.rehancode.finpay.Repository.UserRepository;
import com.rehancode.finpay.Repository.WalletRepository;
import com.rehancode.finpay.Service.AuditService;
import com.rehancode.finpay.Service.LedgerService;
import com.rehancode.finpay.Service.NotificationService;
import com.rehancode.finpay.Service.TransactionService;
import com.rehancode.finpay.Util.SecurityUtil;
import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;


@Slf4j
@Service
public class ITransactionService implements TransactionService {
    private final TransactionRepo transactionRepo;
    private final WalletRepository walletRepository;
    private final UserRepository userRepository;
    private final LedgerService ledgerService;
    private final IdempotencyRecordRepository idempotencyRecordRepository;
    private final NotificationService notificationService;
    private final AuditService auditService;

    public ITransactionService(TransactionRepo transactionRepo, WalletRepository walletRepository, UserRepository userRepository, LedgerService ledgerService, IdempotencyRecordRepository idempotencyRecordRepository, NotificationService notificationService, AuditService auditService) {
        this.transactionRepo = transactionRepo;
        this.walletRepository = walletRepository;
        this.userRepository = userRepository;
        this.ledgerService = ledgerService;
        this.idempotencyRecordRepository = idempotencyRecordRepository;
        this.notificationService = notificationService;
        this.auditService = auditService;
    }

    @Override
    @Transactional
    public ApiResponse<TransferResponseDTO> transfer(TransferRequestDTO transferRequestDTO, String idempotencyKey, String ip, String userAgent) {
            log.info("Transfer Started");
            IdempotencyRecord idempotencyRecord = reserveIdempotencyKey(idempotencyKey);
            Long userId = SecurityUtil.getAuthenticatedUserId();
            log.info("User with id {} authenticated", userId);
            UsersModel sender = userRepository.findById(userId).orElseThrow(() -> new UserNotExists("No User Found"));


//        if(idempotencyRecordRepository.existsByIdempotencyKey(idempotencyKey)){
//            return new ApiResponse<>(HttpStatus.CONFLICT.value(),"Transaction Already Processed",null);
//        }

            WalletModel senderWallet = walletRepository.findByUserId(userId);
            if (senderWallet == null) {
                log.info("User with id {} has no wallet", userId);
                return new ApiResponse<>(HttpStatus.NOT_FOUND.value(), "Sender wallet not found", null);
            }
            UsersModel receiver = userRepository.findById(Long.valueOf(transferRequestDTO.getReceiverUserId())).orElseThrow(() -> new UserNotExists("No User Found"));

            WalletModel receiverWallet = walletRepository.findByUserId(receiver.getId());
            if (receiverWallet == null) {
                log.info("User with id {} has no wallet", receiver.getId());
                return new ApiResponse<>(HttpStatus.NOT_FOUND.value(), "Receiver wallet not found", null);
            }
            if (userId.equals(receiver.getId())) {
                log.warn("cannot transfer to yourself");
                throw new UserNotAllowed("Cannot transfer to yourself");
            }

            BigDecimal senderOldBalance = transactionRepo.findBalanceByWalletId(senderWallet.getId());
            log.info("Sender old balance {} in wallet {}", senderOldBalance,senderWallet.getId());
            BigDecimal receiverOldBalance = transactionRepo.findBalanceByWalletId(receiverWallet.getId());
            log.info("Receiver old balance {} in wallet {}", receiverOldBalance,receiverWallet.getId());

//        if(senderWallet.getBalance().compareTo(transferRequestDTO.getAmount())<0){
//            throw new InsufficientBalanceException("Insufficient Balance");
//        }
            int senderRows = transactionRepo.balanceSend(transferRequestDTO.getAmount(), senderWallet.getId(), Status.ACTIVE);
            log.info("Sender wallet debited updated rows {} with wallet id: {} ", senderRows,senderWallet.getId());
            int receiverRows = transactionRepo.balanceReceive(transferRequestDTO.getAmount(), receiverWallet.getId(), Status.ACTIVE);
            log.info("Receiver wallet credited updated rows {} with wallet id: {}", receiverRows,receiverWallet.getId());
            if (senderRows == 0) {
                log.warn("Insufficient funds in wallet: {}",senderWallet.getId());
                throw new InsufficientBalanceException("Insufficient Balance");
            }

            if (receiverRows == 0) {
                log.warn("Receiver wallet: {} is not active",receiverWallet.getId());
                throw new UserNotAllowed("Receiver wallet is not active");
            }

//        BigDecimal senderNewBalance=transactionRepo.findBalanceByWalletId(senderWallet.getId());
//        BigDecimal receiverNewBalance=transactionRepo.findBalanceByWalletId(receiverWallet.getId());
//        senderWallet.setBalance(senderNewBalance);
//        receiverWallet.setBalance(receiverNewBalance);
//        walletRepository.save(senderWallet);
//        walletRepository.save(receiverWallet);
            log.info("creating transaction and ledger record started for sender wallet: {} and receiver wallet: {}",senderWallet.getId(),receiverWallet.getId());
            createTransactionAndLedger(
                    transferRequestDTO,
                    senderWallet,
                    receiverWallet,
                    senderOldBalance,
                    receiverOldBalance,
                    idempotencyRecord
            );
            log.info("Transaction and ledger record created for sender wallet: {} and receiver wallet: {}",senderWallet.getId(),receiverWallet.getId());
            log.info("creating audit for transfer");
            AuditLogDTO auditLogDTO = new AuditLogDTO();
            auditLogDTO.setUserId(null);
            auditLogDTO.setAction(String.valueOf(TransactionType.TRANSFER));
            auditLogDTO.setEntityId(null);
            auditLogDTO.setEntityType("Transfer Amount");
            auditLogDTO.setStatus(String.valueOf(TransactionStatus.COMPLETED));
            auditLogDTO.setDetails("Amount transferred from " + sender.getUsername() + " to " + receiver.getUsername());
            auditLogDTO.setIpAddress(ip);
            auditLogDTO.setUserAgent(userAgent);
            log.info("audit for transfer started");
            CompletableFuture<Void> auditLog = auditService.auditLog(auditLogDTO);
            log.info("sender email sending started");
            CompletableFuture<Void> senderEmailSend = notificationService.sendEmail(sender.getEmail(), "Amount Debited", "Your account has been debited with amount " + transferRequestDTO.getAmount());
            log.info("receiver email sending started");
            CompletableFuture<Void> receiverEmailSend = notificationService.sendEmail(receiver.getEmail(), "Amount Credited", "Your account has been credited with amount " + transferRequestDTO.getAmount());
            CompletableFuture.allOf(auditLog, senderEmailSend, receiverEmailSend);
            TransferResponseDTO responseDTO = new TransferResponseDTO();
            responseDTO.setAmount(transferRequestDTO.getAmount());
            responseDTO.setReceiverWalletId(receiverWallet.getId());
            responseDTO.setSenderWalletId(senderWallet.getId());
            log.info("Transfer completed");
            return new ApiResponse<>(HttpStatus.OK.value(), "Transfer Success", responseDTO);


    }

    @Override
    public ApiResponse<List<TransactionHistoryDTO>> transferHistory() {

        log.info("Transfer history fetching started");

        Long userId = SecurityUtil.getAuthenticatedUserId();
        log.info("User with id {} authenticated", userId);
        UsersModel user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotExists("No User Found"));

        WalletModel userWallet = walletRepository.findByUser_Id(user.getId());
        log.info("User wallet found {}", userWallet.getId());

        log.info("Fetching transaction for user {}", user.getId());
        List<TransactionModel> transactions =
                transactionRepo.findUserTransactions(userWallet.getId());

        if (transactions.isEmpty()) {
            log.warn("User with id {} has no transactions", userId);
            return new ApiResponse<>(
                    HttpStatus.NOT_FOUND.value(),
                    "No transaction found",
                    null
            );
        }

        List<TransactionHistoryDTO> responseDTO = transactions.stream()
                .map(transaction -> {

                    TransactionHistoryDTO dto = new TransactionHistoryDTO();

                    dto.setAmount(transaction.getAmount());
                    if (transaction.getReceiverWallet() != null) {
                        dto.setReceiverWallet(
                                transaction.getReceiverWallet().getId().toString()
                        );
                    }
                    if (transaction.getSenderWallet() != null) {
                        dto.setSenderWallet(
                                transaction.getSenderWallet().getId().toString()
                        );
                    }

                    dto.setStatus(
                            transaction.getStatus().toString()
                    );
                    dto.setTID(
                            transaction.getReference()
                    );
                    dto.setType(
                            transaction.getType().toString()
                    );
//                    if (transaction.getSenderWallet().getId().equals(userWallet.getId())) {
//                        dto.setDirection("SENT");
//                    } else {
//                        dto.setDirection("RECEIVED");
//                    }
                    if (transaction.getType().equals(TransactionType.DEPOSIT)) {
                        dto.setDirection("DEPOSIT");
                    } else if (transaction.getType().equals(TransactionType.WITHDRAW)) {
                        dto.setDirection("WITHDRAW");
                    } else if (transaction.getSenderWallet() != null
                            && transaction.getSenderWallet().getId().equals(userWallet.getId())) {
                        dto.setDirection("SENT");
                    } else {
                        dto.setDirection("RECEIVED");
                    }

                    return dto;

                })
                .toList();

        log.info("Transfer history completed");
        return new ApiResponse<>(
                HttpStatus.OK.value(),
                "Transfer History",
                responseDTO
        );
    }

    @Override
    public ApiResponse<TransactionHistoryDTO> transferHistoryById(Long id) {
        log.info("Transfer history fetching started with id {}",  id);
        Long userId = SecurityUtil.getAuthenticatedUserId();

        log.info("User with id {} authenticated", userId);

        UsersModel user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotExists("No User Fou8nd"));

        WalletModel walletModel = walletRepository.findByUser_Id(user.getId());
        log.info("User wallet found {}", walletModel.getId());
        TransactionModel transaction =
                transactionRepo.findById(id).orElseThrow(() -> new UserNotExists("No Transaction Found"));


        Long senderID = transaction.getSenderWallet() != null
                ? transaction.getSenderWallet().getId()
                : null;

        Long receiverID = transaction.getReceiverWallet() != null
                ? transaction.getReceiverWallet().getId()
                : null;

//       if(!userId.equals(senderID) &&  !userId.equals(receiverID)){
//           throw new UserNotExists("Transaction does not belong to this user");
//       }
        if (!walletModel.getId().equals(senderID) && !walletModel.getId().equals(receiverID)) {
            log.warn("Transaction does not belong to this user");
            throw new UserNotExists("Transaction does not belong to this user");
        }

        TransactionHistoryDTO dto = new TransactionHistoryDTO();

        dto.setAmount(transaction.getAmount());
        if (transaction.getReceiverWallet() != null) {
            dto.setReceiverWallet(
                    transaction.getReceiverWallet().getId().toString()
            );
        }
        if (transaction.getSenderWallet() != null) {
            dto.setSenderWallet(
                    transaction.getSenderWallet().getId().toString()
            );
        }
        dto.setStatus(
                transaction.getStatus().toString()
        );
        dto.setTID(
                transaction.getReference()
        );
        dto.setType(
                transaction.getType().toString()
        );
        if (transaction.getType().equals(TransactionType.DEPOSIT)) {
            dto.setDirection("DEPOSIT");
        } else if (transaction.getType().equals(TransactionType.WITHDRAW)) {
            dto.setDirection("WITHDRAW");
        } else if (transaction.getSenderWallet() != null
                && transaction.getSenderWallet().getId().equals(walletModel.getId())) {
            dto.setDirection("SENT");
        } else {
            dto.setDirection("RECEIVED");
        }

        log.info("Tranfer history by id completed with id {}",id);
        return new ApiResponse<>(
                HttpStatus.OK.value(),
                "Transfer History",
                dto
        );
    }

    private void createTransactionAndLedger(
            TransferRequestDTO transferRequestDTO,
            WalletModel senderWallet,
            WalletModel receiverWallet,
            BigDecimal senderOldBalance,
            BigDecimal receiverOldBalance,
            IdempotencyRecord idempotencyRecord
    ) {

        String txnId = "TXN_" + UUID.randomUUID();
        log.info("Transaction ID: {} created", txnId);


        TransactionModel transactionModel = new TransactionModel();
        transactionModel.setAmount(transferRequestDTO.getAmount());
        transactionModel.setSenderWallet(senderWallet);
        transactionModel.setReceiverWallet(receiverWallet);
        transactionModel.setType(TransactionType.TRANSFER);
        transactionModel.setReference(txnId);
//        transactionModel.setIdempotencyKey(idempotencyKey);
        transactionModel.setStatus(TransactionStatus.COMPLETED);

        transactionRepo.save(transactionModel);
        log.info("Transaction with ID: {} created", txnId);
        idempotencyRecord.setTransactionModel(transactionModel);
        idempotencyRecord.setStatus("COMPLETED");

        idempotencyRecordRepository.save(idempotencyRecord);
        log.info("Idempotency record saved with status {}", idempotencyRecord.getStatus());
        log.info("ledger saving started");
        ledgerService.addToLedger(
                transactionModel,
                senderWallet,
                receiverWallet,
                senderOldBalance,
                receiverOldBalance
        );
    }

    private IdempotencyRecord reserveIdempotencyKey(String idempotencyKey) {

        IdempotencyRecord record = new IdempotencyRecord();
        log.info("Reserving idempotency key in IdempotencyRecord table");

        record.setIdempotencyKey(idempotencyKey);
        record.setStatus("PROCESSING");

        try {
            IdempotencyRecord savedRecord = idempotencyRecordRepository.saveAndFlush(record);
            log.info("Successfully reserved idempotency key: {}", idempotencyKey); // Log AFTER success
            return savedRecord;


        } catch (DataIntegrityViolationException e) {
            log.error("Error in reserving idempotency key", e);
            throw new DuplicateIdempotencyKeyException(
                    "Idempotency key already processed"
            );
        }
    }
}

package com.rehancode.finpay.Service;

import java.util.concurrent.CompletableFuture;

public interface NotificationService {
    CompletableFuture<Void> sendEmail(String to, String subject, String content);
}

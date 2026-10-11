package com.rehancode.finpay.Service.Impl;

import com.rehancode.finpay.Service.NotificationService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.concurrent.CompletableFuture;

@Slf4j
@Service
public class INotificationService implements NotificationService {
    private final JavaMailSender mailSender;

    public INotificationService(JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }

    @Async("emailExecutor")
    public CompletableFuture<Void> sendEmail(String to, String subject, String content) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(to);
        message.setSubject(subject);
        message.setText(content);
        message.setFrom("admin@finpay.com");
        mailSender.send(message);
        log.info("Sending email to {} completed", to);
        return CompletableFuture.completedFuture(null);
    }
}

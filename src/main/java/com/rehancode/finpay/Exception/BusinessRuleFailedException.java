package com.rehancode.finpay.Exception;

public class BusinessRuleFailedException extends RuntimeException {
    public BusinessRuleFailedException(String message) {
        super(message);
    }
}

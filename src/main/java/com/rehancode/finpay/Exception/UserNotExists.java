package com.rehancode.finpay.Exception;

public class UserNotExists extends RuntimeException {
    public UserNotExists(String message) {
        super(message);
    }

}

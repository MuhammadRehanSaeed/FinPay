package com.rehancode.finpay.Exception;

public class UserNotAllowed extends RuntimeException{
    public UserNotAllowed(String message) {
        super(message);
    }
}

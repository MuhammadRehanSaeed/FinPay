package com.rehancode.finpay.Exception;

public class UserNotAuthenticated extends RuntimeException{
    public UserNotAuthenticated(String message){
        super(message);
    }
}

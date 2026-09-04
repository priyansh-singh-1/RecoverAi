package com.priyanshu.RecoverAi.exception;

public class DuplicatePaymentException extends RuntimeException{
    public DuplicatePaymentException(String message){
        super(message);
    }
}

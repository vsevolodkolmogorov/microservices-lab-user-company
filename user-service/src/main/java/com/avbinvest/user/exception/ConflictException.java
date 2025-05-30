package com.avbinvest.user.exception;

public class ConflictException extends RuntimeException {
    public ConflictException(String text) {
        super(text);
    }
}

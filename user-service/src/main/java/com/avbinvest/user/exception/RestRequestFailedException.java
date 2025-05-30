package com.avbinvest.user.exception;

public class RestRequestFailedException extends RuntimeException {
    public RestRequestFailedException(String text) {
        super(text);
    }
}

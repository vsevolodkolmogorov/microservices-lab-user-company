package com.avbinvest.company.exceptions;

public class RestRequestFailedException extends RuntimeException {
    public RestRequestFailedException(String text) {
        super(text);
    }
}

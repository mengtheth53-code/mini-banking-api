package com.minibanking.api.exception;

public class AccountFrozenException extends RuntimeException {
    public AccountFrozenException(String message) {
        super(message);
    }
}

package com.tomasmendez.picadito.shared.exception;

public class InvalidCredentialsException extends RuntimeException {

    public InvalidCredentialsException() {
        super("Invalid email or password");
    }

    public ErrorCode getErrorCode() {
        return ErrorCode.INVALID_CREDENTIALS;
    }
}

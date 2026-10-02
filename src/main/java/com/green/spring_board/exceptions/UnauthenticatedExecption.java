package com.green.spring_board.exceptions;

public class UnauthenticatedExecption extends RuntimeException {
    public UnauthenticatedExecption(String message) {
        super(message);
    }
}

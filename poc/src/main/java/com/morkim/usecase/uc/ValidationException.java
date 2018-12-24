package com.morkim.usecase.uc;

public class ValidationException extends Exception {

    private int error;

    ValidationException() {

    }

    public ValidationException(int error) {
        this.error = error;
    }

    public int getError() {
        return error;
    }
}

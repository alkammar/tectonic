package com.morkim.tectonic.usecase;

public class UnexpectedStep extends Exception {
    public UnexpectedStep(Throwable cause) {
        super(cause);
    }
}

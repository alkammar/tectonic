package com.morkim.tectonic.usecase;

class UnableToInstantiateUseCase extends RuntimeException {

    UnableToInstantiateUseCase(Throwable cause) {
        super(cause);
    }
}

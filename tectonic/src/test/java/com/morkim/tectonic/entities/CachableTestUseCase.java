package com.morkim.tectonic.entities;

public class CachableTestUseCase extends TestUseCase {

    @Override
    protected boolean isCachable() {
        return true;
    }
}

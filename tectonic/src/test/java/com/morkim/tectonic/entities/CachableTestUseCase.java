package com.morkim.tectonic.entities;

public class CachableTestUseCase extends TestUseCase {

    @Override
    protected boolean supportsCaching() {
        return true;
    }
}

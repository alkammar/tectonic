package com.morkim.tectonic.entities;

import com.morkim.tectonic.Request;

public class CachableTestUseCase extends TestUseCase {

    @Override
    protected boolean supportsCaching() {
        return true;
    }

    @Override
    protected void onUndo(Request request, TestResult oldResult) throws Exception {

        finish();
    }
}

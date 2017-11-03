package com.morkim.tectonic.entities;

import com.morkim.tectonic.Request;

public class CancelledTestUseCase extends TestUseCase {

    @Override
    protected void onExecute(Request request) {
        cancel();
    }
}

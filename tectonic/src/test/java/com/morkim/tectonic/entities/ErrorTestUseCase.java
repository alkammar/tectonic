package com.morkim.tectonic.entities;

import com.morkim.tectonic.Request;

public class ErrorTestUseCase extends TestUseCase {

    @Override
    protected void onExecute(Request request) throws Exception {

//        throw new RuntimeException();

        throw new Exception();
    }
}

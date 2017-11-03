package com.morkim.tectonic.entities;

import com.morkim.tectonic.Request;

public class CreatedTestUseCase extends TestUseCase {

    @Override
    protected void onExecute(Request request) {

        TestResult result = new TestResult();

        updateSubscribers(result);
    }
}

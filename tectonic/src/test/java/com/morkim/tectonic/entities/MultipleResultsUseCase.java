package com.morkim.tectonic.entities;

import com.morkim.tectonic.Request;

public class MultipleResultsUseCase extends TestUseCase {

    @Override
    protected void onExecute(Request request) {

        TestResult result;

        result = new TestResult();
        updateSubscribers(result);

        result = new TestResult();
        updateSubscribers(result);
    }
}

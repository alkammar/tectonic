package com.morkim.tectonic.entities;

import com.morkim.tectonic.Request;
import com.morkim.tectonic.UseCase;

public class CachableTestUseCase extends UseCase<Request, TestResult> {

    private Request request;

    @Override
    protected void onExecute(Request request) throws Exception {
        this.request = request;

        TestResult result = new TestResult();
        result.request = request;

        notifySubscribers(result);

        finish();
    }

    @Override
    protected void onUndo(Request request, TestResult oldResult) throws Exception {

        finish();
    }

    public Request getRequest() {
        return request;
    }
}

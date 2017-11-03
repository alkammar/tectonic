package com.morkim.tectonic.entities;

public class RequestInputTestUseCase extends PendingActionTestUseCase {

    private boolean executionContinued;

    @Override
    protected void onExecute(PendingActionRequest request) {

        if (request == null) {
            requestInput(0);
        } else {

            executionContinued = true;

            TestResult result = new TestResult();

            updateSubscribers(result);

            finish();
        }
    }

    public boolean isExecutionContinued() {
        return executionContinued;
    }
}

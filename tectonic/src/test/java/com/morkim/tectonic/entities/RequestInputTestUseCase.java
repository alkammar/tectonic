package com.morkim.tectonic.entities;

public class RequestInputTestUseCase extends PendingActionTestUseCase {

    public static final int INPUT1 = 1;
    public static final int INPUT2 = 2;

    private boolean executionContinued;

    @Override
    protected void onExecute(PendingActionRequest request) {

        if (request == null) {
            requestInput(0);
        } else {

            if (startInputValidation()
                    .check(request.input1.isEmpty(), INPUT1)
                    .check(request.input2.isEmpty(), INPUT2)
                    .validate()) {

                executionContinued = true;

                TestResult result = new TestResult();

                notifySubscribers(result);

                finish();
            }
        }
    }

    public boolean isExecutionContinued() {
        return executionContinued;
    }
}

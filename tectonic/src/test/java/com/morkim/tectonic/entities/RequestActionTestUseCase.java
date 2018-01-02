package com.morkim.tectonic.entities;

public class RequestActionTestUseCase extends PendingActionTestUseCase {

    public static final int INPUT1 = 1;
    public static final int INPUT2 = 2;

    public static final int ACTOR_1 = 1;
    public static final int ACTOR_2 = 2;

    private boolean executionContinued;

    @Override
    protected void onExecute(PendingActionRequest request) {

        if (request == null) {
            requestAction(ACTOR_1, 0);
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
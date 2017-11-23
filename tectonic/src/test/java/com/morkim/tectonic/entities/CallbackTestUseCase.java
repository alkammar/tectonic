package com.morkim.tectonic.entities;

import com.morkim.tectonic.UseCase;

public class CallbackTestUseCase extends UseCase<PendingActionRequest, TestResult> {

    private boolean isOnExecuteCalled;
    private boolean isOnPostExecuteCalled;

    @Override
    protected void onExecute(PendingActionRequest request) {

        isOnExecuteCalled = true;

        TestResult result = new TestResult();

        updateSubscribers(result);

        finish();
    }

    @Override
    protected void onPostExecute() {

        isOnPostExecuteCalled = true;
    }

    public boolean isOnExecuteCalled() {
        return isOnExecuteCalled;
    }

    public boolean isOnPostExecuteCalled() {
        return isOnPostExecuteCalled;
    }
}

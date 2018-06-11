package com.morkim.tectonic.entities;

import com.morkim.tectonic.Request;

public class ErrorWithPrerequisiteTestUseCase extends TestUseCase {

    private long prerequisiteTimeStamp;
    private int onErrorPrerequisiteCount;

    @Override
    protected void onAddPrerequisites() {

        addPrerequisite(TestUseCase.class);
    }

    @Override
    protected void onExecute(Request request) throws InterruptedException {

        throw new InterruptedException();
    }

    public long getPrerequisiteTimeStamp() {
        return prerequisiteTimeStamp;
    }

    public int getOnErrorPrerequisiteCount() {
        return onErrorPrerequisiteCount;
    }
}

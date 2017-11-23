package com.morkim.tectonic.entities;

import com.morkim.tectonic.SimpleUseCaseListener;

public class CancelPendingPrerequisiteTestUseCase extends TestUseCase {

    private long prerequisiteTimeStamp;
    private int onCancelPrerequisiteCount;

    @Override
    protected void onAddPrerequisites() {

        addPrerequisite(
                PendingActionTestUseCase.class,
                new SimpleUseCaseListener<TestResult>() {
                    @Override
                    public void onCancel() {
                        prerequisiteTimeStamp = System.nanoTime();
                        onCancelPrerequisiteCount++;
                    }
                });
    }

    public long getPrerequisiteTimeStamp() {
        return prerequisiteTimeStamp;
    }

    public int getOnCancelPrerequisiteCount() {
        return onCancelPrerequisiteCount;
    }
}

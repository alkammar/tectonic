package com.morkim.tectonic.entities;

import com.morkim.tectonic.SimpleUseCaseListener;

public class ErrorPrerequisiteTestUseCase extends TestUseCase {

    private long prerequisiteTimeStamp;
    private int onErrorPrerequisiteCount;

    @Override
    protected void onAddPrerequisites() {

        addPrerequisite(
                ErrorTestUseCase.class,
                new SimpleUseCaseListener<TestResult>() {
                    @Override
                    public boolean onError(Throwable throwable) {
                        prerequisiteTimeStamp = System.nanoTime();
                        onErrorPrerequisiteCount++;
                        return false;
                    }
                });
    }

    public long getPrerequisiteTimeStamp() {
        return prerequisiteTimeStamp;
    }

    public int getOnErrorPrerequisiteCount() {
        return onErrorPrerequisiteCount;
    }
}

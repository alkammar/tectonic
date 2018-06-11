package com.morkim.tectonic.entities;

import com.morkim.tectonic.SimpleUseCaseListener;

public class HasPrerequisitesTestUseCase extends TestUseCase {

    private long prerequisite1TimeStamp;
    private long prerequisite2TimeStamp;
    private long prerequisite3TimeStamp;

    @Override
    protected void onAddPrerequisites() {

        addPrerequisite(
                PrerequisiteTestUseCase.class,
                new SimpleUseCaseListener<TestResult>() {
                    @Override
                    public void onComplete() {
                        prerequisite1TimeStamp = System.nanoTime();
                    }
                });

//        addPrerequisite(
//                new Precondition() {
//                    @Override
//                    public boolean onEvaluate() {
//                        return false;
//                    }
//                },
//                TestUseCase.class,
//                new SimpleUseCaseListener<TestResult>() {
//                    @Override
//                    public void onComplete() {
//                        prerequisite2TimeStamp = System.nanoTime();
//                    }
//                });
//
//        addPrerequisite(
//                new Precondition() {
//                    @Override
//                    public boolean onEvaluate() {
//                        return true;
//                    }
//                },
//                TestUseCase.class,
//                new SimpleUseCaseListener<TestResult>() {
//                    @Override
//                    public void onComplete() {
//                        prerequisite3TimeStamp = System.nanoTime();
//                    }
//                });

        addPrerequisite(
                TestUseCase.class);
    }

    public long getPrerequisite1TimeStamp() {
        return prerequisite1TimeStamp;
    }

    public long getPrerequisite2TimeStamp() {
        return prerequisite2TimeStamp;
    }

    public long getPrerequisite3TimeStamp() {
        return prerequisite3TimeStamp;
    }
}

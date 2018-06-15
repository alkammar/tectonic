package com.morkim.tectonic.simplified;

import org.junit.Before;
import org.junit.BeforeClass;

public class ConcurrentTectonicTest {

    Thread useCaseThread;

    private ThreadManagerImpl threadManager;

    @BeforeClass
    public static void setupClass() {

    }

    @Before
    public void setup() {
        UseCase.clearAll();
        threadManager = new ThreadManagerImpl() {
            @Override
            public void start(UseCaseExecution execution) {
                super.start(execution);

                ConcurrentTectonicTest.this.useCaseThread = threadManager.thread;
            }
        };
        UseCase.defaultThreadManager(threadManager);
    }

    void sleep() {
        try {
            Thread.sleep(100);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}

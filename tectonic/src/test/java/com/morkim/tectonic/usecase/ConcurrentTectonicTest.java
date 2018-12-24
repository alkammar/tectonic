package com.morkim.tectonic.usecase;

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
        sleep(100);
    }

    void sleep(long delay) {
        try {
            Thread.sleep(delay);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}

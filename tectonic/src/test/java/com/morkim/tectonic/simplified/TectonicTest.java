package com.morkim.tectonic.simplified;

import org.junit.Before;
import org.junit.BeforeClass;

public class TectonicTest {

    @BeforeClass
    public static void setupClass() {

    }

    @Before
    public void setup() {
        UseCase.clearAll();
        UseCase.defaultThreadManager(new ThreadManager() {
            @Override
            public boolean isRunning() {
                return true;
            }

            @Override
            public void release() throws InterruptedException {

            }

            @Override
            public void start(UseCaseExecution execution) {
                try {
                    execution.run();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void stop() {

            }

            @Override
            public void restart() {

            }
        });
    }
}

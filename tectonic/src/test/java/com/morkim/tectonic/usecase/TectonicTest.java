package com.morkim.tectonic.usecase;

import org.junit.Before;
import org.junit.BeforeClass;

public abstract class TectonicTest {

    @BeforeClass
    public static void setupClass() {

    }

    @Before
    public void setup() {
        UseCase.clearAll();
        UseCase.setGlobalThreadManager(new ThreadManager() {
            @Override
            public boolean isRunning() {
                return true;
            }

            @Override
            public void stop() throws InterruptedException {

            }

            @Override
            public void complete() {

            }

            @Override
            public void start(UseCaseExecution execution) {
                try {
                    execution.run();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                } catch (UndoException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void finish() {

            }

            @Override
            public void restart() {

            }
        });
    }
}

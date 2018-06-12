package com.morkim.tectonic.simplified;

public interface ThreadManager {

    boolean isRunning();

    interface UseCaseExecution {

        void run() throws InterruptedException;
    }

    void start(UseCaseExecution execution);

    void stop();

    void restart();
}

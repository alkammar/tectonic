package com.morkim.tectonic.usecase;

public interface ThreadManager {

    boolean isRunning();

    void release() throws InterruptedException;

    interface UseCaseExecution {

        void run() throws InterruptedException;

        void stop();
    }

    void start(UseCaseExecution execution);

    void stop();

    void restart();
}

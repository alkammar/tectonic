package com.morkim.tectonic.usecase;

public interface ThreadManager {

    boolean isRunning();

    void release() throws InterruptedException;

    void complete();

    void start(UseCaseExecution execution);

    void stop();

    void restart();

    interface UseCaseExecution {

        void run() throws InterruptedException, UndoException;

        void onStop();

        void onDestroy();

        void onComplete() throws InterruptedException;
    }
}

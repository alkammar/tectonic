package com.morkim.tectonic.usecase;

public interface ThreadManager {

    boolean isRunning();

    void stop();

    void complete();

    void start(UseCaseExecution execution);

    void finish();

    void restart();

    interface UseCaseExecution {

        void run() throws InterruptedException, UndoException;

        void onStop();

        void onDestroy();

        void onComplete() throws InterruptedException;
    }
}

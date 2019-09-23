package com.morkim.tectonic.usecase;

public class ThreadManagerImpl implements ThreadManager {

    Thread thread;

    private volatile boolean running;
    private volatile boolean terminated;
    private volatile boolean completed;
    private final Object terminationLock = new Object();

    @Override
    public boolean isRunning() {
        return running && !terminated;
    }

    @Override
    public void start(final UseCaseExecution execution) {

        running = true;
        if (!terminated)
            thread = new Thread(new Runnable() {
                @Override
                public void run() {
                    while (isRunning()) {
                        synchronized (terminationLock) {
                            try {
                                execution.run();
                                terminationLock.wait();
                            } catch (InterruptedException e) {
                                if (terminated) {
                                    execution.onStop();
                                } else if (completed) {
                                    try {
                                        execution.onComplete();
                                    } catch (InterruptedException e1) {
                                        // Do nothing as we are already interrupted
                                    }
                                }
                            } catch (UndoException e) {
                                // catch this to allow re-executing the use case
                            }
                        }
                    }

                    execution.onDestroy();
                }
            });

        thread.start();
    }

    @Override
    public void finish() {
        terminated = true;
        thread.interrupt();
    }

    @Override
    public void restart() {
        thread.interrupt();
    }

    @Override
    public void stop() {
        running = false;
        if (thread != null) thread.interrupt();
//        terminationLock.wait();
    }

    @Override
    public void complete() {
        completed = true;
        thread.interrupt();
    }
}

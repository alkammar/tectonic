package com.morkim.tectonic.usecase;

public class ThreadManagerImpl implements ThreadManager {

    Thread thread;

    private volatile boolean running;
    private volatile boolean terminated;
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
                                    execution.stop();
                                    break;
                                }
                            } catch (UndoException e) {
                                // catch this to allow re-executing the use case
                            }
                        }
                    }

                    execution.terminate();
                }
            });

        thread.start();
    }

    @Override
    public void stop() {
        terminated = true;
        thread.interrupt();
    }

    @Override
    public void restart() {
        thread.interrupt();
    }

    @Override
    public void release() throws InterruptedException {
        terminationLock.wait();
    }
}

package com.morkim.tectonic.simplified;

public class ThreadManagerImpl implements ThreadManager {

    private Thread thread;
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
                                if (terminated) break;
                            }
                        }
                    }
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
}

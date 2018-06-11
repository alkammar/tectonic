package com.morkim.tectonic.simplified;

public class ThreadManagerImpl implements ThreadManager {
    @Override
    public void start(Runnable runnable) {
        new Thread(runnable).start();
    }
}

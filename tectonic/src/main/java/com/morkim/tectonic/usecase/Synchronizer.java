package com.morkim.tectonic.usecase;

import com.google.common.util.concurrent.SettableFuture;

import java.util.concurrent.ExecutionException;

@SuppressWarnings("WeakerAccess")
public class Synchronizer<D> {

    private SettableFuture<D> future = SettableFuture.create();
    private Thread thread = Thread.currentThread();

    public D get() throws ExecutionException, InterruptedException {
        return future.get();
    }

    public void interrupt() {
        thread.interrupt();
    }

    public void set(D data) {
        future.set(data);
    }

    public void setException(Exception e) {
        future.setException(e);
    }
}

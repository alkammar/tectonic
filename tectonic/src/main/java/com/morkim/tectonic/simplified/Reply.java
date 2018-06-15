package com.morkim.tectonic.simplified;

import com.google.common.util.concurrent.SettableFuture;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutionException;

public class Reply<D> {

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
}

package com.morkim.tectonic.simplified;

import com.google.common.util.concurrent.SettableFuture;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutionException;

public class Reply {

    private static Map<Integer, Object> cache = new HashMap<>();

    public static <D> SettableFuture<D> create() {
        return SettableFuture.create();
    }

    public static <D> D block(SettableFuture<D> future) throws InterruptedException {

        try {
            return future.get();
        } catch (ExecutionException e) {
            throw new RuntimeException(e.getCause());
        }
    }

    public static <D> void unblock(SettableFuture<D> future, D data) {
        future.set(data);
    }

    public static <D> void unblock(SettableFuture<D> future, D data, int key) {
        cache.put(key, data);
        unblock(future, data);
    }

    public static <D> D block(SettableFuture<D> future, int key) throws InterruptedException {

        Object o = cache.get(key);
        if (o != null) return (D) o;
        return block(future);
    }
}

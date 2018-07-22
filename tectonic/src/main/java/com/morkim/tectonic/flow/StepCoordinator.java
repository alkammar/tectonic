package com.morkim.tectonic.flow;

import com.google.common.util.concurrent.SettableFuture;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutionException;

public class StepCoordinator {

    private static volatile Map<Integer, SettableFuture> futureMap = new HashMap<>();

    public static synchronized <T> T waitFor(int key) throws InterruptedException {

        while (!futureMap.containsKey(key));

        SettableFuture<T> future = SettableFuture.create();
        futureMap.put(key, future);
        try {
            return future.get();
        } catch (ExecutionException e) {
            if (e.getCause() instanceof InterruptedException)
                throw (InterruptedException) e.getCause();
            throw new RuntimeException();
        }
    }

    public static <T> void replyWith(Integer key, T step) {
        SettableFuture<T> future = futureMap.get(key);
        if (future != null) {
            future.set(step);
            futureMap.remove(future);
        }
    }

    public static void clear(Integer key) {

    }
}

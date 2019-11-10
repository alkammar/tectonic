package com.morkim.tectonic.flow;

import android.util.Log;

import com.google.common.util.concurrent.SettableFuture;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutionException;

public class StepCoordinator {

    private static final Map<Integer, SettableFuture> futureMap = new HashMap<>();
    private static final Map<Integer, Object> cache = new HashMap<>();
    private static Set<Step> discarded = new HashSet<>();

    public static synchronized <T> T waitFor(int key) throws InterruptedException {

        Log.d("StepCoordinator", "futureMap.size() " + futureMap.size() + " " + key);

        while (futureMap.containsKey(key)) ;

        SettableFuture<T> future;
        synchronized (futureMap) {
            if (cache.containsKey(key)) {
                Log.d("StepCoordinator", "cache.containsKey(key) " + key);
                T t = (T) cache.get(key);
                cache.remove(key);
                return t;
            } else {
                Log.d("StepCoordinator", "cache.containsKey(key) else " + key);
                future = SettableFuture.create();
                futureMap.put(key, future);
            }
        }
        try {
            Log.d("StepCoordinator", "future.get() " + key);
            return future.get();
        } catch (ExecutionException e) {
            if (e.getCause() instanceof InterruptedException)
                throw (InterruptedException) e.getCause();
            throw new RuntimeException();
        }
    }

    public static <T> void replyWith(Integer key, T step) {
        synchronized (futureMap) {
            SettableFuture<T> future = futureMap.get(key);
            if (future != null) {
                Log.d("StepCoordinator", "future != null " + key);
                future.set(step);
                futureMap.remove(key);
            } else if (discarded.contains(step)) {
                Log.d("StepCoordinator", "discarded.contains(step) " + key);
                discarded.remove(step);
            } else {
                Log.d("StepCoordinator", "future == null " + key);
                cache.put(key, step);
            }
        }
    }

    public static void clear(Integer key) {

    }

    public static void discard(Step step) {
        discarded.add(step);
    }
}

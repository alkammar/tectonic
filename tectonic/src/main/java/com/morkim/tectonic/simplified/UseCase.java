package com.morkim.tectonic.simplified;

import android.annotation.SuppressLint;

import com.google.common.util.concurrent.SettableFuture;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutionException;

@SuppressLint("UseSparseArrays")
public abstract class UseCase {

    private static Map<Class<? extends UseCase>, UseCase> created = new HashMap<>();
    private static ThreadManager defaultThreadManager;
    private static Map<Integer, Reply> replies = new HashMap<>();
    private static Map<Integer, Object> cache = new HashMap<>();
    private boolean running;
    private Map<Integer, Object> steps;

    private ThreadManager threadManager = new ThreadManagerImpl();

    public synchronized static <U extends UseCase> U fetch(Class<U> useCaseClass) {

        //noinspection unchecked
        U useCase = (U) created.get(useCaseClass);
        if (useCase == null) {
            try {
                useCase = useCaseClass.newInstance();
                created.put(useCaseClass, useCase);
            } catch (Exception e) {
                throw new UnableToInstantiateUseCase(e.getCause());
            }
        }

        return useCase;
    }

    @SuppressLint("UseSparseArrays")
    protected UseCase() {
        steps = new HashMap<>();
    }

    protected boolean onCheckPreconditions() {
        return true;
    }

    public void execute() {

        if (created.containsKey(getClass()) && !running) {
            running = true;
            getThreadManager().start(new ThreadManager.UseCaseExecution() {
                @Override
                public void run() throws InterruptedException {

                    if (onCheckPreconditions())
                        onExecute();
                }
            });
        }
    }

    private ThreadManager getThreadManager() {
        return defaultThreadManager == null ? threadManager : defaultThreadManager;
    }

    protected abstract void onExecute() throws InterruptedException;

    protected <D> D step(int key, CacheDataListener<D> listener) {
        if (steps.containsKey(key)) return (D) steps.get(key);
        D newData = listener.onNewData();
        steps.put(key, newData);
        return newData;
    }

    public static void defaultThreadManager(ThreadManager threadManager) {
        defaultThreadManager = threadManager;
    }

    public static <D> D immediate(D data) {
        return data;
    }

    public static <D> D waitFor(int key) throws InterruptedException {
        if (cache.containsKey(key))
            return (D) cache.get(key);
        else {
            Reply<D> reply = new Reply<>();
            replies.put(key, reply);
            try {
                return reply.get();
            } catch (ExecutionException e) {
                e.printStackTrace();
            }

            return null;
        }
    }

    public static <D> void replyWith(int key, D data) {
        Reply reply = replies.get(key);
        Object cachedData = cache.get(key);

        if (reply != null && cachedData != null)
            reply.interrupt();
        else {
            cache.put(key, data);
            if (reply != null) reply.set(data);
        }
    }

    protected interface CacheDataListener<D> {
        D onNewData();
    }

    protected void finish() {
        created.remove(getClass());
        getThreadManager().stop();
    }

    public static void clearAll() {
        created.clear();
        cache.clear();
        replies.clear();
    }

    public void restart() {
        running = false;
        execute();
    }
}

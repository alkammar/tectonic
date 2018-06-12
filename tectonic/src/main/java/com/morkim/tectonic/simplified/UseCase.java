package com.morkim.tectonic.simplified;

import android.annotation.SuppressLint;

import java.util.HashMap;
import java.util.Map;

public abstract class UseCase {

    private static Map<Class<? extends UseCase>, UseCase> created = new HashMap<>();
    private static ThreadManager defaultThreadManager;
    private boolean running;
    private Map<Integer, Object> cache;

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
        cache = new HashMap<>();
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

    protected abstract void onExecute();

    protected <D> D cache(int key, CacheDataListener<D> listener) {
        if (cache.containsKey(key)) return (D) cache.get(key);
        D newData = listener.onNewData();
        cache.put(key, newData);
        return newData;
    }

    public static void defaultThreadManager(ThreadManager threadManager) {
        defaultThreadManager = threadManager;
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
    }

    public void restart() {
        running = false;
        execute();
    }
}

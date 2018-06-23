package com.morkim.tectonic.simplified;

import android.annotation.SuppressLint;

import com.morkim.tectonic.flow.Step;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutionException;

@SuppressLint("UseSparseArrays")
public abstract class UseCase<E, R> implements PreconditionActor<E, R>, UseCaseHandle {

    private static Map<Class<? extends UseCase>, UseCase> created = new HashMap<>();
    private static Map<Thread, ThreadManager> waitingUndo = new HashMap<>();
    private static ThreadManager defaultThreadManager;
    private static Map<Integer, Reply> replies = new HashMap<>();
    private static Map<Integer, Object> cache = new HashMap<>();
    private boolean running;
    private Map<Integer, Object> steps;

    private ThreadManager threadManager = new ThreadManagerImpl();
    private PrimaryActor<E, R> primaryActor;
    private PreconditionActor<E, R> preconditionActor;

    private Triggers<E> triggers;
    private E event;

    public synchronized static <U extends UseCase> U fetch(Class<U> useCaseClass) {

        //noinspection unchecked
        U useCase = (U) created.get(useCaseClass);
        if (useCase == null) {
            try {
                useCase = useCaseClass.newInstance();
                created.put(useCaseClass, useCase);
                useCase.onCreate();
            } catch (Exception e) {
                throw new UnableToInstantiateUseCase(e.getCause());
            }
        }

        return useCase;
    }

    protected void onCreate() {

    }

    @SuppressLint("UseSparseArrays")
    protected UseCase() {
        steps = new HashMap<>();
    }

    protected boolean onCheckPreconditions() {
        return true;
    }

    @SafeVarargs
    protected final void triggerPreconditions(E... events) {
        for (E event : events) triggers.trigger(event);
    }

    public void execute() {
        execute(null);
    }

    public void execute(E event) {
        this.event = event;

        if (created.containsKey(getClass()) && !running) {
            running = true;
            getThreadManager().start(new ThreadManager.UseCaseExecution() {
                @Override
                public void run() throws InterruptedException {

                    if (onCheckPreconditions()) {
                        if (primaryActor != null) primaryActor.onStart(UseCase.this);
                        onExecute();
                    }
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

    public static <D> D immediate(D data) throws InterruptedException {
        Thread currentThread = Thread.currentThread();
        if (waitingUndo.containsKey(currentThread)) {
            ThreadManager threadManager = waitingUndo.get(currentThread);
            waitingUndo.remove(currentThread);
            threadManager.release();
        }

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

        if (reply != null && cachedData != null) {
            cache.put(key, data);
            reply.interrupt();
        } else {
            cache.put(key, data);
            if (reply != null) reply.set(data);
        }
    }

    public static void clear(int key) {
        cache.remove(key);
    }

    public UseCase<E, R> setPrimaryActor(PrimaryActor<E, R> primaryActor) {
        this.primaryActor = primaryActor;

        return this;
    }

    public UseCase setPreconditionActor(PreconditionActor<E, R> preconditionActor) {
        this.preconditionActor = preconditionActor;

        return this;
    }

    public UseCase<E, R> setTriggers(Triggers<E> triggers) {
        this.triggers = triggers;

        return this;
    }

    protected interface CacheDataListener<D> {
        D onNewData();
    }

    protected void complete() {
        complete(null);
    }

    protected void complete(R result) {
        if (running && preconditionActor != null) preconditionActor.onComplete(event, result);
        if (preconditionActor != primaryActor)
            if (running && primaryActor != null) primaryActor.onComplete(event, result);
        running = false;
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
        execute(event);
    }

    @Override
    public void undo(Step step, int... actions) {
        ThreadManager threadManager = getThreadManager();
        waitingUndo.put(Thread.currentThread(), threadManager);
        for (int action : actions) cache.remove(action);
        threadManager.restart();

        if (running && primaryActor != null) primaryActor.onUndo(step);
    }

    @Override
    public void abort() {
        if (running && preconditionActor != null) preconditionActor.onAbort(event);
        if (preconditionActor != primaryActor)
            if (running && primaryActor != null) primaryActor.onAbort(event);
    }

    @Override
    public void onComplete(E event, R result) {

    }

    @Override
    public void onAbort(E event) {

    }
}

package com.morkim.tectonic.usecase;

import android.annotation.SuppressLint;

import com.morkim.tectonic.flow.Step;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutionException;

@SuppressLint("UseSparseArrays")
public abstract class UseCase<E, R> implements PreconditionActor<E>, UseCaseHandle {

    private static Map<Class<? extends UseCase>, UseCase> created = new HashMap<>();
    private static Map<Thread, ThreadManager> waitingUndo = new HashMap<>();
    private static ThreadManager defaultThreadManager;
    private static Map<Integer, Reply> replies = new HashMap<>();
    private static Map<Integer, Object> cache = new HashMap<>();
    private boolean running;
    private Map<Integer, Object> steps;

    private ThreadManager threadManager = new ThreadManagerImpl();
    private PrimaryActor<E, R> primaryActor;
    private PreconditionActor<E> preconditionActor;

    private Triggers<E> triggers;
    private E event;
    private volatile Set<E> preconditions = new HashSet<>();
    private ResultActor<E, R> resultActor;

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

    public void execute() {
        execute(null);
    }

    public void execute(final E event) {
        this.event = event;

        if (created.containsKey(getClass()) && !running) {
            running = true;
            getThreadManager().start(new ThreadManager.UseCaseExecution() {
                @Override
                public void run() throws InterruptedException {

                    waitForPreconditions();
                    if (primaryActor != null) primaryActor.onStart(UseCase.this);
                    onExecute();
                }

                @Override
                public void stop() {
                    if (running) {
                        if (preconditionActor != null) preconditionActor.onAbort(event);
                        if (resultActor != null) resultActor.onAbort(event);
                        if (preconditionActor != primaryActor)
                            if (primaryActor != null) primaryActor.onAbort(event);
                        running = false;
                    }
                }
            });
        }
    }

    private void waitForPreconditions() {
        onAddPreconditions(preconditions);
        for (E event : preconditions) triggers.trigger(event, this);
        //noinspection StatementWithEmptyBody
        while (preconditions.size() > 0) ;
    }

    protected void onAddPreconditions(Set<E> events) {

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

    public static void replyWith(int key) {
        replyWith(key, null);
    }

    public static <D> void replyWith(int key, D data) {
        Reply reply = replies.get(key);
        Object cachedData = cache.get(key);

        if (reply != null && cachedData != null) {
            cache.put(key, data);
            reply.interrupt();
        } else {
            cache.put(key, data);
//            replies.remove(key);
            if (reply != null) reply.set(data);
        }
    }

    public static void clear(int... keys) {
        for (int key : keys) cache.remove(key);
    }

    public void setPrimaryActor(PrimaryActor<E, R> primaryActor) {
        this.primaryActor = primaryActor;

    }

    public void setPreconditionActor(PreconditionActor<E> preconditionActor) {
        this.preconditionActor = preconditionActor;
    }

    public void setResultActor(ResultActor<E, R> resultActor) {
        this.resultActor = resultActor;
    }

    public void setTriggers(Triggers<E> triggers) {
        this.triggers = triggers;
    }

    public Builder builder() {
        return new Builder();
    }

    protected interface CacheDataListener<D> {
        D onNewData();
    }

    protected void complete() {
        complete(null);
    }

    protected void complete(R result) {
        if (running && preconditionActor != null) preconditionActor.onComplete(event);
        if (running && resultActor != null) resultActor.onComplete(event, result);
        if (preconditionActor != primaryActor && resultActor != primaryActor)
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
        created.remove(getClass());
        getThreadManager().stop();
    }

    @Override
    public void onComplete(E event) {
        preconditions.remove(event);
    }

    @Override
    public void onAbort(E event) {

    }
}

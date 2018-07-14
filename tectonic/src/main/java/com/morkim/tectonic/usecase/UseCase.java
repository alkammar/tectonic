package com.morkim.tectonic.usecase;

import android.annotation.SuppressLint;

import com.morkim.tectonic.flow.Step;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ExecutionException;

@SuppressLint("UseSparseArrays")
public abstract class UseCase<E, R> implements PreconditionActor<E>, UseCaseHandle {

    private Triggers<E> executor;

    private static Map<Class<? extends UseCase>, UseCase> created = new HashMap<>();
    private static Map<Thread, ThreadManager> waitingUndo = new HashMap<>();
    private static ThreadManager defaultThreadManager;
    private Map<UUID, Action> actions = new HashMap<>();
    private static Map<UUID, Thread> keyThreadMap = new HashMap<>();
    private static Map<Thread, UseCase> threadUseCaseMap = new HashMap<>();
    private static Map<UUID, Object> cache = new HashMap<>();
    private static boolean waitingToRestart;
    private Action<?> blockingAction;
    private boolean running;
    private Map<Integer, Object> steps;


    private ThreadManager threadManager = new ThreadManagerImpl();
    private PrimaryActor<E, R> primaryActor;
    private ResultActor<E, R> resultActor;
    private PreconditionActor<E> preconditionActor;

    private E event;
    private volatile Set<E> preconditions = new HashSet<>();
    private boolean preconditionsExecuted;
    private volatile boolean aborted;

    public synchronized static <U extends UseCase> U fetch(Class<U> useCaseClass) {

        //noinspection unchecked
        U useCase = (U) created.get(useCaseClass);
        if (useCase == null) {
            try {
                useCase = useCaseClass.newInstance();
                created.put(useCaseClass, useCase);
                useCase.onCreate();
            } catch (Exception e) {
                e.printStackTrace();
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
        execute((E) null);
    }

    public void execute(final E event) {
        this.event = event;

        if (created.containsKey(getClass()) && !running) {
            running = true;
            getThreadManager().start(new ThreadManager.UseCaseExecution() {
                @Override
                public void run() throws InterruptedException {
                    threadUseCaseMap.put(Thread.currentThread(), UseCase.this);
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

                @Override
                public void terminate() {
                    threadUseCaseMap.remove(Thread.currentThread());
                    for (UUID key : actions.keySet()) {
                        keyThreadMap.remove(key);
                    }
                    actions.clear();
                }
            });
        }
    }

    protected <r> r execute(Class<? extends UseCase<E, r>> cls) throws AbortedUseCase, InterruptedException {
        return execute(null, cls);
    }

    protected <r> r execute(UUID key, Class<? extends UseCase<E, r>> cls) throws AbortedUseCase, InterruptedException {
        r result = (cache.containsKey(key)) ? (r) cache.get(key) : executor.trigger(cls, event);
        if (key != null) cache.put(key, result);
        return result;
    }

    private void waitForPreconditions() {
        if (!preconditionsExecuted) onAddPreconditions(preconditions);
        for (E event : preconditions) executor.trigger(event, this);
        //noinspection StatementWithEmptyBody
        while (preconditions.size() > 0 && !aborted);
        preconditionsExecuted = true;
        if (aborted) abort();
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

    public static <D> Random<D> waitForRandom(UUID key) {
        if (cache.containsKey(key))
            return (Random<D>) cache.get(key);
        else {
            return new Random<>();
        }
    }

    public static <D> D waitFor(UUID key) throws InterruptedException, ExecutionException {
        if (cache.containsKey(key))
            return (D) cache.get(key);
        else {
            Action<D> action = new Action<>();
            UseCase useCase = threadUseCaseMap.get(Thread.currentThread());
            useCase.actions.put(key, action);
            keyThreadMap.put(key, Thread.currentThread());
            useCase.blockingAction = action;
            try {
                return action.get();
            } catch (ExecutionException e) {
                if (e.getCause() instanceof InterruptedException) throw (InterruptedException) e.getCause();
                throw e;
            }
        }
    }

    public static <D> D waitForSafe(UUID key) throws InterruptedException {
        try {
            return waitFor(key);
        } catch (ExecutionException e) {
            e.printStackTrace();
        }

        return null;
    }

    @SafeVarargs
    public static <D> D waitFor(UUID key, Class<? extends Exception>... exs) throws InterruptedException, UnexpectedStep {

        if (cache.containsKey(key))
            return (D) cache.get(key);
        else {
            Action<D> action = new Action<>();
            UseCase useCase = threadUseCaseMap.get(Thread.currentThread());
            useCase.actions.put(key, action);
            keyThreadMap.put(key, Thread.currentThread());
            useCase.blockingAction = action;
            try {
                return action.get();
            } catch (ExecutionException e) {
                for (Class<? extends Exception> ex : exs)
                    if (e.getCause().getClass() == ex) //noinspection unchecked
                        throw new UnexpectedStep(e.getCause());
            }
        }

        return null;
    }

    public static void replyWith(UUID key) {
        replyWith(key, null);
    }

    public static <D> void replyWith(UUID key, D data) {
        Thread thread = keyThreadMap.get(key);
        Action action = thread == null ? null : (Action) threadUseCaseMap.get(thread).actions.get(key);
        Object cachedData = cache.get(key);

        if (action != null && cachedData != null) {
            cache.put(key, data);
            action.interrupt();
        } else if (data instanceof Exception) {
            if (action != null) action.setException((Exception) data);
        } else {
            cache.put(key, data);
            if (action != null) action.set(data);
        }
    }

    public static void replyWithRandom(UUID key) {
        replyWithRandom(key, null);
    }

    public static <D> void replyWithRandom(UUID key, D data) {

        UseCase useCase = threadUseCaseMap.get(keyThreadMap.get(key));
        Action action = (Action) useCase.actions.get(key);
        if (action == useCase.blockingAction) {
            cache.put(key, data);
            useCase.blockingAction.interrupt();
            useCase.blockingAction = null;
        } else {
            replyWith(key, data);
        }
    }

    public static void clear(UUID... keys) {
        for (UUID key : keys) cache.remove(key);
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

    public void setExecutor(Triggers<E> executor) {
        this.executor = executor;
    }

    public Builder builder() {
        return new Builder();
    }

    protected interface CacheDataListener<D> {
        D onNewData();
    }

    protected void complete() throws InterruptedException {
        complete(null);
    }

    protected void complete(R result) throws InterruptedException {

        if (waitingToRestart) {
            waitingToRestart = false;
            throw new InterruptedException();
        }

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
//        actions.clear();
    }

    public void retry() {
        retry(false);
    }

    public void retry(boolean withPreconditions) {
        running = false;
        preconditionsExecuted = !withPreconditions;
        execute(event);
    }

    @Override
    public void undo(Step step, UUID... actions) {
        ThreadManager threadManager = getThreadManager();
        waitingUndo.put(Thread.currentThread(), threadManager);
        for (UUID action : actions) cache.remove(action);
        threadManager.restart();

        if (running && primaryActor != null) primaryActor.onUndo(step);
    }

    @Override
    public void abort() {
        if (preconditionsExecuted) {
            created.remove(getClass());
            getThreadManager().stop();
        } else {
            aborted = true;
        }
    }

    @Override
    public void onComplete(E event) {
        preconditions.remove(event);
    }

    @Override
    public void onAbort(E event) {

    }
}

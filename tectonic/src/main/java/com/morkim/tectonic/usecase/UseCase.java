package com.morkim.tectonic.usecase;

import android.annotation.SuppressLint;

import com.morkim.tectonic.flow.Step;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutionException;


@SuppressWarnings({"WeakerAccess", "unused"})
@SuppressLint("UseSparseArrays")
public abstract class UseCase<R> implements PreconditionActor {

    public static final Step ANONYMOUS_STEP = new Step() {
        @Override
        public void terminate() {

        }
    };

    private Triggers<?> executor;

    private static final Map<Class<? extends UseCase>, UseCase> ALIVE = new ConcurrentHashMap<>();
    private static ThreadManager defaultThreadManager;
    private Map<UUID, Action> actions = new HashMap<>();
    private Thread thread;
    private StepCache cache = new StepCache();
    private static boolean waitingToRestart;

    private Action<?> blockingAction;
    private boolean running;
    private Map<Integer, Object> steps;


    private ThreadManager threadManager = new ThreadManagerImpl();
    private Set<PrimaryActor> primaryActors = new LinkedHashSet<>();
    private Set<SecondaryActor> secondaryActors = new LinkedHashSet<>();
    private Set<ResultActor<TectonicEvent, R>> resultActors = new HashSet<>();
    private PreconditionActor preconditionActor;

    private Set<Class<? extends UseCase>> completingWhenCompletedSet = new HashSet<>();
    private Set<Class<? extends UseCase>> abortingWhenCompletedSet = new HashSet<>();
    private Set<Class<? extends UseCase>> completingWhenAbortedSet = new HashSet<>();
    private Set<Class<? extends UseCase>> abortingWhenAbortedSet = new HashSet<>();

    private TectonicEvent event;
    private volatile Set<Class<? extends UseCase<?>>> preconditions = new HashSet<>();
    private volatile Map<TectonicEvent, Class<? extends UseCase<?>>> preconditionEvents = new HashMap<>();
    private volatile boolean preconditionsExecuted;
    private volatile boolean aborted;
    private final UseCaseHandle primaryHandle;
    private final UseCaseHandle secondaryHandle;

    public synchronized static <U extends UseCase> U fetch(Class<U> useCaseClass) {

        U useCase;
        synchronized (ALIVE) {
            //noinspection unchecked
            useCase = (U) ALIVE.get(useCaseClass);
            if (useCase == null) {
                try {
                    useCase = useCaseClass.newInstance();
                    ALIVE.put(useCaseClass, useCase);
                    useCase.onCreate();
                } catch (Exception e) {
                    e.printStackTrace();
                    throw new UnableToInstantiateUseCase(e.getCause());
                }
            }
        }

        return useCase;
    }

    protected void onCreate() {

    }

    @SuppressLint("UseSparseArrays")
    protected UseCase() {
        steps = new HashMap<>();

        primaryHandle = new Handle();
        secondaryHandle = new Handle() {
            @Override
            public void abort() {

            }
        };
    }

    public void execute() {
        execute((TectonicEvent) null);
    }

    public void execute(final TectonicEvent event) {
        this.event = event;

        if (ALIVE.containsKey(getClass()) && !running) {

            running = true;

            getThreadManager().start(new ThreadManager.UseCaseExecution() {
                @Override
                public void run() throws InterruptedException, UndoException {

                    UseCase.this.thread = Thread.currentThread();
                    completeWhenCompleted(completingWhenCompletedSet);
                    abortWhenCompleted(abortingWhenCompletedSet);
                    completeWhenAborted(completingWhenAbortedSet);
                    abortWhenAborted(abortingWhenAbortedSet);

                    boolean executeOnStart = !preconditionsExecuted;
                    waitForPreconditions();

                    onInitialize();

                    primaryActors.clear();
                    secondaryActors.clear();

                    onAddSecondaryActors(secondaryActors);
                    onAddPrimaryActors(primaryActors);

                    if (executeOnStart) notifyActorsOfStart(event);

                    try {
                        onExecute();
                    } catch (UndoException e) {
                        if (running) notifyActorsOfUndo(e);
                        throw e;
                    }
                }

                @Override
                public void onStop() {
                    if (running) {
                        if (preconditionActor != null) preconditionActor.onAbort(event);

                        for (ResultActor<TectonicEvent, R> resultActor : resultActors)
                            if (resultActor != null) resultActor.onAbort(event);

                        notifyActorsOfAbort(event);

                        running = false;

                        completeWhenAborted(UseCase.this);
                        abortWhenAborted(UseCase.this);
                    }
                }

                @Override
                public void onComplete() throws InterruptedException {
                    UseCase.this.complete();
                }

                @Override
                public void onDestroy() {
                    thread = null;
                    actions.clear();
                    cache.clear();
                }
            });
        }
    }

    protected void onInitialize() {

    }

    protected <r> r execute(Class<? extends UseCase<r>> cls) throws AbortedUseCase, InterruptedException {
        return execute(null, cls);
    }

    protected <r> r execute(UUID key, Class<? extends UseCase<r>> cls) throws AbortedUseCase, InterruptedException {
        r result = (cache.containsKey(key)) ? (r) cache.get(key) : executor.trigger(cls, event);
        if (key != null) cache.put(ANONYMOUS_STEP, key, result);
        return result;
    }

    private void waitForPreconditions() throws InterruptedException, UndoException {
        if (!preconditionsExecuted) onAddPreconditions(preconditions);
        for (Class<? extends UseCase<?>> precondition : preconditions)
            preconditionEvents.put(executor.trigger(precondition, this), precondition);
        //noinspection StatementWithEmptyBody
        while (preconditions.size() > 0 && !aborted) ;
        preconditionsExecuted = true;
        if (aborted) {
            abort();
            waitForSafe(UUID.randomUUID());
        }
    }

    protected void onAddPreconditions(Set<Class<? extends UseCase<?>>> useCases) {

    }

    private ThreadManager getThreadManager() {
        return defaultThreadManager == null ? threadManager : defaultThreadManager;
    }

    protected abstract void onAddPrimaryActors(Set<PrimaryActor> actors);

    protected abstract void onAddSecondaryActors(Set<SecondaryActor> actors);

    protected abstract void onExecute() throws InterruptedException, UndoException;

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

    private  <D> Random<D> waitForRandom(UUID key) {
        return cache.containsKey(key) ? (Random<D>) cache.get(key) : new Random<D>();
    }

    private <D> D waitForSafe(UUID key) throws InterruptedException, UndoException {
        try {
            return waitFor(key);
        } catch (ExecutionException e) {
            e.printStackTrace();
        }

        return null;
    }

    private <D> D waitFor(UUID key) throws InterruptedException, ExecutionException, UndoException {

        if (cache.containsKey(key)) {
            D d = cache.get(key);
            if (d instanceof Exception) {
                cache.remove(key);
                throw new ExecutionException((Throwable) d);
            }
            return d;
        } else {
            Action<D> action = new Action<>();
            actions.put(key, action);
            blockingAction = action;
            try {
                return action.get();
            } catch (ExecutionException e) {
                if (e.getCause() instanceof InterruptedException)
                    throw (InterruptedException) e.getCause();
                if (UndoException.class.equals(e.getCause().getClass()))
                    throw (UndoException) e.getCause();
                throw e;
            }
        }
    }

    private <D> D waitFor(UUID key, Runnable runnable) throws InterruptedException, ExecutionException {

        if (cache.containsKey(key)) {
            D d = cache.get(key);
            if (d instanceof Exception) {
                cache.remove(key);
                throw new ExecutionException((Throwable) d);
            }
            return d;
        } else {

            runnable.run();

            Action<D> action = new Action<>();
            actions.put(key, action);
            blockingAction = action;
            try {
                return action.get();
            } catch (ExecutionException e) {
                if (e.getCause() instanceof InterruptedException)
                    throw (InterruptedException) e.getCause();
                throw e;
            }
        }
    }

    @SuppressWarnings("unchecked")
    private <D> D waitFor(UUID key, Class<? extends Exception>... exs) throws InterruptedException, UnexpectedStep {

        if (cache.containsKey(key)) {
            return cache.get(key);
        } else {
            Action<D> action = new Action<>();

            actions.put(key, action);
            blockingAction = action;

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

    private void replyWith(Step step, UUID key) {
        replyWith(step, key, null);
    }

    private <D> void replyWith(Step step, UUID key, D data) {

        Action action = thread == null ? null : this.actions.get(key);
        Object cachedData = cache.get(key);

        if (action != null && cachedData != null) {
            cache.put(step, key, data);
            action.interrupt();
        } else if (data instanceof Exception) {
            if (action != null) action.setException((Exception) data);
        } else {
            cache.put(step, key, data);
            if (action != null) action.set(data);
        }
    }

    private void replyWithRandom(Step step, UUID key) {
        replyWithRandom(step, key, null);
    }

    private <D> void replyWithRandom(Step step, UUID key, D data) {

        Action action = (Action) actions.get(key);
        if (action == blockingAction) {
            cache.put(step, key, data);
            blockingAction.interrupt();
            blockingAction = null;
        } else {
            replyWith(step, key, data);
        }
    }

    public void setPreconditionActor(PreconditionActor preconditionActor) {
        this.preconditionActor = preconditionActor;
    }

    public void addResultActor(ResultActor<TectonicEvent, R> resultActor) {
        this.resultActors.add(resultActor);
    }

    public void setExecutor(Triggers<?> executor) {
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
        if (running) {
            for (ResultActor<TectonicEvent, R> resultActor : resultActors) {
                if (resultActor != null) resultActor.onComplete(event, result);
            }
            notifyActorsOfComplete(result);
        }

        running = false;
        preconditionsExecuted = false;
        onDestroy();
        ALIVE.remove(getClass());
        getThreadManager().stop();

        completeWhenCompleted(this);
        abortWhenCompleted(this);
    }

    private void notifyActorsOfStart(TectonicEvent event) {

        for (Actor actor : primaryActors)
            if (actor != null) actor.onStart(event, primaryHandle);

        for (Actor actor : secondaryActors)
            if (actor != null) actor.onStart(event, secondaryHandle);
    }

    private void notifyActorsOfUndo(UndoException e) {
        for (Actor actor : primaryActors)
            if (actor != null) actor.onUndo(e.getStep());

        for (Actor actor : secondaryActors)
            if (actor != null) actor.onUndo(e.getStep());
    }

    private void notifyActorsOfComplete(R result) {
        for (PrimaryActor actor : primaryActors)
            if (preconditionActor != actor && !resultActors.contains(actor) && actor != null)
                actor.onComplete(event, result);
        for (SecondaryActor actor : secondaryActors)
            if (preconditionActor != actor && !resultActors.contains(actor) && actor != null)
                actor.onComplete(event, result);
    }

    private void notifyActorsOfAbort(TectonicEvent event) {
        for (Actor actor : primaryActors)
            if (preconditionActor != actor && actor != null) actor.onAbort(event);

        for (Actor actor : secondaryActors)
            if (preconditionActor != actor && actor != null) actor.onAbort(event);
    }

    private void completeWhenCompleted(UseCase<R> uc) {

        synchronized (ALIVE) {
            List<UseCase> useCases = new ArrayList<>(ALIVE.values());
            for (UseCase useCase : useCases) {
                if (uc != useCase && useCase.completingWhenCompletedSet.contains(uc.getClass()))
                    useCase.getThreadManager().complete();
            }
        }
    }

    private void abortWhenCompleted(UseCase<R> uc) {

        synchronized (ALIVE) {
            List<UseCase> useCases = new ArrayList<>(ALIVE.values());
            for (UseCase useCase : useCases) {
                if (uc != useCase && useCase.abortingWhenCompletedSet.contains(uc.getClass()))
                    useCase.abort();
            }
        }
    }

    private void completeWhenAborted(UseCase<R> uc) {

        synchronized (ALIVE) {
            List<UseCase> useCases = new ArrayList<>(ALIVE.values());
            for (UseCase useCase : useCases) {
                if (uc != useCase && useCase.completingWhenAbortedSet.contains(uc.getClass()))
                    useCase.getThreadManager().complete();
            }
        }
    }

    private void abortWhenAborted(UseCase<R> uc) {

        synchronized (ALIVE) {
            List<UseCase> useCases = new ArrayList<>(ALIVE.values());
            for (UseCase useCase : useCases) {
                if (uc != useCase && useCase.abortingWhenAbortedSet.contains(uc.getClass()))
                    useCase.abort();
            }
        }
    }

    public void clear(UUID... keys) {
        for (UUID key : keys) cache.remove(key);
    }

    public static void clearAll() {

        for (UseCase useCase : ALIVE.values())
            useCase.cache.clear();

        ALIVE.clear();
    }

    public void retry() {
        retry(false);
    }

    @SuppressWarnings("WeakerAccess")
    protected void retry(@SuppressWarnings("SameParameterValue") boolean withPreconditions) {
        running = false;
        preconditionsExecuted = !withPreconditions;
        execute(event);
    }

    private void undo(Step step) {
        cache.remove(step);

        if (blockingAction != null) blockingAction.setException(new UndoException(step));
    }

    protected void abort() {
        if (preconditionsExecuted) {
            synchronized (ALIVE) {
                onDestroy();
                ALIVE.remove(getClass());
            }
            getThreadManager().stop();
        } else {
            aborted = true;
        }
    }

    protected void onDestroy() {

    }

    @Override
    public void onComplete(TectonicEvent event) {
        preconditions.remove(preconditionEvents.get(event));
        preconditionEvents.remove(event);
    }

    @Override
    public void onAbort(TectonicEvent event) {

    }

    protected void completeWhenCompleted(Set<Class<? extends UseCase>> by) {

    }

    protected void abortWhenCompleted(Set<Class<? extends UseCase>> by) {

    }

    protected void completeWhenAborted(Set<Class<? extends UseCase>> by) {

    }

    protected void abortWhenAborted(Set<Class<? extends UseCase>> by) {

    }

    private class Handle implements UseCaseHandle {

        @Override
        public void undo(Step step) {
            UseCase.this.undo(step);
        }

        @Override
        public void abort() {
            UseCase.this.abort();
        }

        @Override
        public <D> Random<D> waitForRandom(UUID key) {
            return UseCase.this.waitForRandom(key);
        }

        @Override
        public <D> D waitFor(UUID key) throws ExecutionException, UndoException, InterruptedException {
            return UseCase.this.waitFor(key);
        }

        @Override
        public <D> D waitForSafe(UUID key) throws UndoException, InterruptedException {
            return UseCase.this.waitForSafe(key);
        }

        @Override
        public <D> D waitFor(UUID key, Runnable runnable) throws InterruptedException, ExecutionException {
            return UseCase.this.waitFor(key, runnable);
        }

        @Override
        public <D> D waitFor(UUID key, Class<? extends Exception>... exs) throws UnexpectedStep, InterruptedException {
            return UseCase.this.waitFor(key, exs);
        }

        @Override
        public void replyWithRandom(Step step, UUID key) {
            UseCase.this.replyWithRandom(step, key);
        }

        @Override
        public void replyWith(Step step, UUID key) {
            UseCase.this.replyWith(step, key);
        }

        @Override
        public <D> void replyWith(Step step, UUID key, D data) {
            UseCase.this.replyWith(step, key, data);
        }

        @Override
        public <D> void replyWithRandom(Step step, UUID key, Random<D> data) {
            UseCase.this.replyWithRandom(step, key, data);
        }

        @Override
        public void clear(UUID... keys) {
            UseCase.this.clear(keys);
        }
    }
}

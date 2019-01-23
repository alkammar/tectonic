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
    private Thread thread;
    private StepCache cache = new StepCache();
    private static boolean waitingToRestart;

    private Synchronizer<?> blockingSynchronizer;
    private boolean running;

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

                    onInitialize();

                    primaryActors.clear();
                    secondaryActors.clear();

                    onAddSecondaryActors(secondaryActors);
                    onAddPrimaryActors(primaryActors);

                    boolean executeOnStart = !preconditionsExecuted;
                    waitForPreconditions();

                    if (executeOnStart) notifyActorsOfStart(event);

                    // TODO move the above calls to a new method that is only called once
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

    protected <r> r execute(final UUID key, final Class<? extends UseCase<r>> cls) throws AbortedUseCase, InterruptedException {

        if (cache.containsKey(key)) {
            return cache.getValue(key);
        } else {
            Triggers<TectonicEvent> triggers = (Triggers<TectonicEvent>) executor;

            final UUID finalKey = key == null ? UUID.randomUUID() : key;
            triggers.trigger(
                    triggers.map(cls, event),
                    null,
                    null,
                    new ResultActor<TectonicEvent, r>() {
                        @Override
                        public void onComplete(TectonicEvent event, r result) {
                            replyWith(finalKey, result);
                        }

                        @Override
                        public void onAbort(TectonicEvent event) {
                            replyWith(finalKey, new AbortedUseCase(cls));
                        }
                    },
                    event);
            try {
                return waitFor(null, ANONYMOUS_STEP, finalKey, AbortedUseCase.class);
            } catch (UnexpectedStep e) {
                if (e.getCause() instanceof AbortedUseCase) throw (AbortedUseCase) e.getCause();
            }
        }

        return null;
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
            waitForSafe(null, ANONYMOUS_STEP, UUID.randomUUID());
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

    public static void defaultThreadManager(ThreadManager threadManager) {
        defaultThreadManager = threadManager;
    }

    public static <D> D immediate(D data) {
        return data;
    }

    private <D> Random<D> waitForRandom(UUID key) {
        return cache.containsKey(key) ? (Random<D>) cache.getValue(key) : new Random<D>();
    }

    private <D> D waitForSafe(Actor actor, Step step, UUID key) throws InterruptedException, UndoException {
        try {
            return waitFor(actor, step, key);
        } catch (ExecutionException e) {
            e.printStackTrace();
        }

        return null;
    }

    private <D> D waitFor(Actor actor, Step step, UUID key) throws InterruptedException, ExecutionException, UndoException {

        if (cache.containsKey(key)) {
            D d = cache.getValue(key);
            if (d instanceof Exception) {
                cache.remove(key);
                throw new ExecutionException((Throwable) d);
            }
            return d;
        } else {
            Synchronizer<D> synchronizer = new Synchronizer<>();
            cache.put(actor, step, key, synchronizer);
            blockingSynchronizer = synchronizer;
            try {
                return synchronizer.get();
            } catch (ExecutionException e) {
                if (e.getCause() instanceof InterruptedException) throw (InterruptedException) e.getCause();
                if (UndoException.class.equals(e.getCause().getClass())) throw (UndoException) e.getCause();
                throw e;
            }
        }
    }

    private <D> D waitFor(Actor actor, Step step, UUID key, Runnable runnable) throws InterruptedException, ExecutionException, UndoException {

        if (cache.containsKey(key)) {
            D d = cache.getValue(key);
            if (d instanceof Exception) {
                cache.remove(key);
                throw new ExecutionException((Throwable) d);
            }
            return d;
        } else {

            runnable.run();

            Synchronizer<D> synchronizer = new Synchronizer<>();
            cache.put(actor, step, key, synchronizer);
            blockingSynchronizer = synchronizer;
            try {
                return synchronizer.get();
            } catch (ExecutionException e) {
                if (e.getCause() instanceof InterruptedException) throw (InterruptedException) e.getCause();
                if (UndoException.class.equals(e.getCause().getClass())) throw (UndoException) e.getCause();
                throw e;
            }
        }
    }

    @SuppressWarnings("unchecked")
    private <D> D waitFor(Actor actor, Step step, UUID key, Class<? extends Exception>... exs) throws InterruptedException, UnexpectedStep {

        if (cache.containsKey(key)) {
            return cache.getValue(key);
        } else {
            Synchronizer<D> synchronizer = new Synchronizer<>();

            cache.put(actor, step, key, synchronizer);
            blockingSynchronizer = synchronizer;

            try {
                return synchronizer.get();
            } catch (ExecutionException e) {
                for (Class<? extends Exception> ex : exs)
                    if (e.getCause().getClass() == ex) //noinspection unchecked
                        throw new UnexpectedStep(e.getCause());
            }
        }

        return null;
    }

    private void replyWith(UUID key) {
        replyWith(key, null);
    }

    private <D> void replyWith(UUID key, D data) {

        Synchronizer synchronizer = thread == null ? null : cache.getAction(key);
        Object cachedData = cache.getValue(key);

        if (synchronizer != null && cachedData != null) {
            cache.put(key, data);
            synchronizer.interrupt();
        } else if (data instanceof Exception) {
            if (synchronizer != null) synchronizer.setException((Exception) data);
        } else {
            cache.put(key, data);
            if (synchronizer != null) synchronizer.set(data);
        }
    }

    private void replyWithRandom(UUID key) {
        replyWithRandom(key, null);
    }

    private <D> void replyWithRandom(UUID key, D data) {

        Synchronizer synchronizer = cache.getAction(key);
        if (synchronizer == blockingSynchronizer) {
            cache.put(key, data);
            blockingSynchronizer.interrupt();
            blockingSynchronizer = null;
        } else {
            replyWith(key, data);
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

    @SuppressWarnings("SuspiciousMethodCalls")
    private void notifyActorsOfUndo(UndoException e) {

        Step step = cache.peak();
        Actor actor = cache.pop();
        actor.onUndo(step, true);
        if (primaryActors.contains(actor)) {
            Actor original = actor;
            step = cache.peak();
            while (step != null) {
                actor = cache.getActor(step);
                if (!primaryActors.contains(actor)) {
                    cache.pop();
                    actor.onUndo(step, true);
                    step = cache.peak();
                } else if (actor == original) {
                    cache.reset(step);
                    break;
                }
            }

        } else if (!primaryActors.contains(actor)) {
            Actor original = actor;
            boolean isPrimary;
            do {
                step = cache.peak();
                if (step == null) break;
                actor = cache.pop();
                isPrimary = primaryActors.contains(actor);
                actor.onUndo(step, !isPrimary);
            } while (isPrimary);
        }
    }

    private void notifyActorsOfComplete(R result) {
        for (PrimaryActor actor : primaryActors)
            if (preconditionActor != actor && !resultActors.contains(actor) && actor != null)
                actor.onComplete(event);
        for (SecondaryActor actor : secondaryActors)
            if (preconditionActor != actor && !resultActors.contains(actor) && actor != null)
                actor.onComplete(event);
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

    private void undo() {
        if (!cache.isEmpty())
            if (blockingSynchronizer != null)
                blockingSynchronizer.setException(new UndoException());
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
        public void undo() {
            UseCase.this.undo();
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
        public <D> D waitFor(Actor actor, UUID key) throws ExecutionException, UndoException, InterruptedException {
            return UseCase.this.waitFor(actor, new IsolatedStep(), key);
        }

        @Override
        public <D> D waitFor(Actor actor, Step step, UUID key) throws ExecutionException, UndoException, InterruptedException {
            return UseCase.this.waitFor(actor, step, key);
        }

        @Override
        public <D> D waitForSafe(Actor actor, UUID key) throws UndoException, InterruptedException {
            return UseCase.this.waitForSafe(actor, new IsolatedStep(), key);
        }

        @Override
        public <D> D waitForSafe(Actor actor, Step step, UUID key) throws UndoException, InterruptedException {
            return UseCase.this.waitForSafe(actor, step, key);
        }

        @Override
        public <D> D waitFor(Actor actor, UUID key, Runnable runnable) throws InterruptedException, ExecutionException, UndoException {
            return UseCase.this.waitFor(actor, new IsolatedStep(), key, runnable);
        }

        @Override
        public <D> D waitFor(Actor actor, Step step, UUID key, Runnable runnable) throws InterruptedException, ExecutionException, UndoException {
            return UseCase.this.waitFor(actor, step, key, runnable);
        }

        @Override
        public <D> D waitFor(Actor actor, UUID key, Class<? extends Exception>... exs) throws UnexpectedStep, InterruptedException {
            return UseCase.this.waitFor(actor, new IsolatedStep(), key, exs);
        }

        @Override
        public <D> D waitFor(Actor actor, Step step, UUID key, Class<? extends Exception>... exs) throws UnexpectedStep, InterruptedException {
            return UseCase.this.waitFor(actor, step, key, exs);
        }

        @Override
        public void replyWithRandom(UUID key) {
            UseCase.this.replyWithRandom(key);
        }

        @Override
        public void replyWith(UUID key) {
            UseCase.this.replyWith(key);
        }

        @Override
        public <D> void replyWith(UUID key, D data) {
            UseCase.this.replyWith(key, data);
        }

        @Override
        public <D> void replyWithRandom(UUID key, Random<D> data) {
            UseCase.this.replyWithRandom(key, data);
        }

        @Override
        public void clear(UUID... keys) {
            UseCase.this.clear(keys);
        }
    }

    private class IsolatedStep implements Step {

        @Override
        public void terminate() {

        }
    }
}

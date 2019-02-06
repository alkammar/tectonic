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

import javax.annotation.Nonnull;


@SuppressWarnings({"WeakerAccess", "unused"})
@SuppressLint("UseSparseArrays")
public abstract class UseCase<R> implements PreconditionActor {

    private static final Step ANONYMOUS_STEP = new Step() {
        @Override
        public void terminate() {

        }
    };

    private static final PrimaryActor ANONYMOUS_ACTOR = new PrimaryActor() {
        @Override
        public void onStart(Object event, UseCaseHandle handle) {

        }

        @Override
        public void onUndo(Step step, boolean inclusive) {

        }

        @Override
        public void onComplete(Object event) {

        }

        @Override
        public void onAbort(Object event) {

        }
    };


    private Triggers<?> executor;

    private static final Map<Class<? extends UseCase>, UseCase> ALIVE = new ConcurrentHashMap<>();

    private Thread thread;
    private StepCache cache = new StepCache();

    private Synchronizer<?> blockingSynchronizer;
    private boolean running;

    private ThreadManager threadManager;
    private ThreadManager defaultThreadManager = new ThreadManagerImpl();

    private Set<PrimaryActor> primaryActors = new LinkedHashSet<>();
    private Set<SecondaryActor> secondaryActors = new LinkedHashSet<>();
    private Set<ResultActor<TectonicEvent, R>> resultActors = new HashSet<>();
    private PreconditionActor preconditionActor;

    private Set<Class<? extends UseCase>> completingWhenCompletedSet = new HashSet<>();
    private Set<Class<? extends UseCase>> abortingWhenCompletedSet = new HashSet<>();
    private Set<Class<? extends UseCase>> completingWhenAbortedSet = new HashSet<>();
    private Set<Class<? extends UseCase>> abortingWhenAbortedSet = new HashSet<>();

    private TectonicEvent event;
    private volatile Set<Class<? extends UseCase>> preconditions = new HashSet<>();
    private volatile Map<TectonicEvent, Class<? extends UseCase>> preconditionEvents = new HashMap<>();
    private volatile boolean preconditionsExecuted;

    private volatile boolean aborted;

    private final UseCaseHandle primaryHandle;
    private final UseCaseHandle secondaryHandle;
    private static ThreadManager globalThreadManager;
    private UseCase container;
    private ResultActor<TectonicEvent, R> containerResultActor;

    /**
     * Returns the current (only) instance that is alive of this {@code useCaseClass}. If no instance
     * is running it will create one and returns it. If an exception is thrown during the creation of
     * the use case a {@link UnableToInstantiateUseCase} runtime exception is thrown wrapping the
     * original exception.
     *
     * @param useCaseClass the use case class to fetch
     * @param <U> the use case type
     * @return the use case instance
     */
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
                System.out.println("Secondary actors are not allowed to abort a use case");
            }
        };
    }

    /**
     * Version of {@link #execute(TectonicEvent)} without an event, which will associate this use case
     * execution with a null event.
     */
    public void execute() {
        execute((TectonicEvent) null);
    }

    /**
     * Executes the use case associating it with the {@code event}. This event is going to be passed
     * in all the callbacks for all the actors. This will start with a call to {@link Actor#onStart(Object, UseCaseHandle)}
     * and ends up with a call to either {@link Actor#onComplete(Object)} or {@link Actor#onAbort(Object)}
     * for primary and secondary actors. {@link ResultActor#onComplete(Object, Object)} or {@link ResultActor#onAbort(Object)}
     * will be called for all result actors.
     * Executing a use case while it is still alive will do nothing, as in order to execute the use
     * case again the use case thread has to terminate by either a completion or an abortion.
     *
     * @param event the event that triggered the use case
     */
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

                    if (container != null) {
                        completingWhenCompletedSet.add(container.getClass());
                        abortingWhenAbortedSet.add(container.getClass());
                    }

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
                    }
                }

                @Override
                public void onStop() {
                    if (running) {
                        if (preconditionActor != null) //noinspection unchecked
                            preconditionActor.onAbort(event);

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

    /**
     *
     */
    protected void onInitialize() {

    }

    /**
     * Version of {@link #execute(UUID, Class)} without a key. In this case the use case will generate
     * its key for this step.
     */
    protected <r> r execute(Class<? extends UseCase<r>> cls) throws UndoException, InterruptedException {
        return execute(null, cls);
    }

    /**
     * Executes another use case {@code cls} as a sub use case, blocking the current use case thread
     * with the key {@code key}. If the sub use case was aborted an {@link UndoException} will be thrown.
     * Once the sub use case is completed the current use case will resume its execution. The sub use
     * case is considered a step in the use execution and will be assigned internally an anonymous actor
     * and step.
     *
     * @param key the blocking key
     * @param cls the sub use case class
     * @param <r> the use case result type
     * @return the use case result
     * @throws UndoException thrown if the sub use case was aborted
     * @throws InterruptedException if the use case thread was interrupted while waiting for the
     * sub use case to finish
     */
    protected <r> r execute(final UUID key, final Class<? extends UseCase<r>> cls) throws UndoException, InterruptedException {

        if (cache.containsKey(key)) {
            return cache.getValue(key);
        } else {
            //noinspection unchecked
            Triggers<TectonicEvent> triggers = (Triggers<TectonicEvent>) executor;

            final UUID finalKey = key == null ? UUID.randomUUID() : key;
            ResultActor<TectonicEvent, ?> subResultActor = new ResultActor<TectonicEvent, r>() {
                @Override
                public void onComplete(TectonicEvent event, r result) {
                    replyWith(finalKey, result);
                }

                @Override
                public void onAbort(TectonicEvent event) {
                    replyWith(finalKey, new UndoException());
                }
            };

            final UseCase<?> useCase = new Builder()
                    .useCase(cls)
                    .containerResultActor(subResultActor)
                    .container(this)
                    .triggers(executor)
                    .build();

            PrimaryActor subPrimaryActor = new PrimaryActor() {
                @Override
                public void onStart(Object event, UseCaseHandle handle) {

                }

                @Override
                public void onUndo(Step step, boolean inclusive) {
                    useCase.reset();
                    useCase.getThreadManager().restart();
                }

                @Override
                public void onComplete(Object event) {

                }

                @Override
                public void onAbort(Object event) {

                }
            };

            primaryActors.add(subPrimaryActor);

            useCase.execute();

            try {
                return waitFor(subPrimaryActor, ANONYMOUS_STEP, finalKey);
            } catch (ExecutionException e) {
                if (e.getCause() instanceof UndoException) throw (UndoException) e.getCause();
            }
        }

        return null;
    }

    private void waitForPreconditions() throws InterruptedException, UndoException {
        if (!preconditionsExecuted) onAddPreconditions(preconditions);
        //noinspection unchecked
        Triggers<TectonicEvent> triggers = (Triggers<TectonicEvent>) executor;
        for (Class<? extends UseCase> precondition : preconditions)
            preconditionEvents.put(triggers.trigger(triggers.map(precondition), this, null, event), precondition);

        //noinspection StatementWithEmptyBody
        while (preconditions.size() > 0 && !aborted) ;
        preconditionsExecuted = true;
        if (aborted) {
            abort();
            waitForSafe(ANONYMOUS_ACTOR, ANONYMOUS_STEP, UUID.randomUUID());
        }
    }

    /**
     * Called before {@link #onExecute()} to add the precondition use cases that will run before this
     * use case starts to execute.
     *
     * @param useCases the precondition set to add the use case class to
     */
    protected void onAddPreconditions(Set<Class<? extends UseCase>> useCases) {

    }

    private ThreadManager getThreadManager() {
        return threadManager == null ? globalThreadManager == null ? defaultThreadManager : globalThreadManager : threadManager;
    }

    protected void setThreadManager(ThreadManager threadManager) {
        this.threadManager = threadManager;
    }

    static void setGlobalThreadManager(ThreadManager threadManager) {
        globalThreadManager = threadManager;
    }

    /**
     * Called before {@link #onExecute()} to add the primary actors to this use case. Actors added here
     * will be able receive the callbacks for the {@link Actor} interface.
     *
     * @param actors the primary actors set to add the actor to
     */
    protected abstract void onAddPrimaryActors(Set<PrimaryActor> actors);

    /**
     * Called before {@link #onExecute()} to add the secondary actors to this use case. Actors added here
     * will be able receive the callbacks for the {@link Actor} interface.
     *
     * @param actors the secondary actors set to add the actor to
     */
    protected abstract void onAddSecondaryActors(Set<SecondaryActor> actors);

    /**
     * Called when the use case starts to execute. You should write here all your use case business
     * logic, including the main scenario and the alternate scenarios. This can be executed multiple
     * times based on your implementation choices for the actors (e.g. using {@link UseCaseHandle#replyWithRandom(UUID, Random)})
     * or the undo scenarios.
     *
     * @throws InterruptedException
     * @throws UndoException
     */
    protected abstract void onExecute() throws InterruptedException, UndoException;

    @SuppressWarnings("UnusedReturnValue")
    public static <D> D immediate(D data) {
        return data;
    }

    private <D> Random<D> waitForRandom(UUID key) {
        //noinspection unchecked
        return cache.containsKey(key) ? (Random<D>) cache.getValue(key) : new Random<D>();
    }

    private <D> D waitForSafe(@Nonnull Actor actor, Step step, UUID key) throws InterruptedException, UndoException {
        try {
            return waitFor(actor, step, key);
        } catch (ExecutionException e) {
            e.printStackTrace();
        }

        return null;
    }

    private <D> D waitFor(@Nonnull Actor actor, Step step, UUID key) throws InterruptedException, ExecutionException, UndoException {

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
                if (e.getCause() instanceof InterruptedException)
                    throw (InterruptedException) e.getCause();
                if (UndoException.class.equals(e.getCause().getClass()))
                    throw (UndoException) e.getCause();
                throw e;
            }
        }
    }

    private <D> D waitFor(@Nonnull Actor actor, Step step, UUID key, Runnable runnable) throws InterruptedException, ExecutionException, UndoException {

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
                if (e.getCause() instanceof InterruptedException)
                    throw (InterruptedException) e.getCause();
                if (UndoException.class.equals(e.getCause().getClass()))
                    throw (UndoException) e.getCause();
                throw e;
            }
        }
    }

    @SuppressWarnings("unchecked")
    private <D> D waitFor(@Nonnull Actor actor, Step step, UUID key, Class<? extends Exception>... exs) throws InterruptedException, UnexpectedStep {

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

        //noinspection unchecked
        Synchronizer<D> synchronizer = thread == null ? null : cache.getSynchronizer(key);
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

        Synchronizer synchronizer = cache.getSynchronizer(key);
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

    void setContainer(UseCase container) {
        this.container = container;
    }

    public void setContainerResultActor(ResultActor<TectonicEvent, R> containerResultActor) {
        this.containerResultActor = containerResultActor;
    }

    protected interface CacheDataListener<D> {
        D onNewData();
    }

    /**
     * Version of {@link #onExecute()} that does not cause the precondition use cases to be re-executed
     */
    public void retry() throws InterruptedException {
        retry(false);
    }

    /**
     * Forces the {@link #onExecute()} to be called again which will execute until it reaches the first
     * blocking call by an actor. This method is typically used when handling alternate error scenarios
     * in the use case to give actors another chance to alter their response. Error scenarios can be
     * for example UI actor entered incorrect data or a secondary actor service that thrown an exception.
     *
     * @param withPreconditions if true causes the precondition use cases to be re-executed
     * @throws InterruptedException always thrown to signal the thread to re-enter its loop resulting
     * in a call to {@link #onExecute()}
     */
    @SuppressWarnings("WeakerAccess")
    protected void retry(@SuppressWarnings("SameParameterValue") boolean withPreconditions) throws InterruptedException {
        preconditionsExecuted = !withPreconditions;
        throw new InterruptedException();
    }

    private void undo() {
        if (!cache.isEmpty())
            if (blockingSynchronizer != null)
                blockingSynchronizer.setException(new UndoException());
    }

    @SuppressWarnings("SuspiciousMethodCalls")
    private void reset() {
        Step step = cache.peak();
        Actor actor = cache.getActor(step);
        if (primaryActors.contains(actor)) {
            cache.reset(step);
        } else if (!primaryActors.contains(actor)) {
            cache.pop();

            Actor original = actor;
            step = cache.peak();
            actor = cache.getActor(step);
            boolean isPrimary = primaryActors.contains(actor);

            while (!isPrimary) {
                cache.pop();
                step = cache.peak();
                if (step == null) break;
                actor = cache.getActor(step);
                isPrimary = primaryActors.contains(actor);
            }

            if (step != null) cache.reset(step);
        }
    }

    /**
     * Version of {@link #complete(Object)} without a result value
     */
    protected void complete() {
        complete(null);
    }

    /**
     * Completes a use case execution triggering the {@link Actor#onComplete(Object)} and {@link ResultActor#onComplete(Object, Object)}
     * callbacks and termination of the use case thread. The result is passed to all observing result
     * actors. The completion of a use case will trigger the completion of abortion of other use cases
     * added via {@link #completeWhenCompleted(Set)} and {@link #abortWhenCompleted(Set)}
     *
     * @param result the use case result
     *
     * @see PrimaryActor
     * @see SecondaryActor
     * @see ResultActor
     */
    protected void complete(R result) {

        if (container != null && ALIVE.containsKey(container.getClass())) {
            if (running) {
                //noinspection unchecked
                containerResultActor.onComplete(event, result);
            }
        } else {
            if (running && preconditionActor != null) //noinspection unchecked
                preconditionActor.onComplete(event);
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

    private void notifyActorsOfStart(TectonicEvent event) {

        for (Actor actor : secondaryActors)
            if (actor != null) //noinspection unchecked
                actor.onStart(event, secondaryHandle);

        for (Actor actor : primaryActors)
            if (actor != null) //noinspection unchecked
                actor.onStart(event, primaryHandle);
    }

    @SuppressWarnings("SuspiciousMethodCalls")
    private void notifyActorsOfUndo(UndoException e) throws UndoException {

        Step step = cache.peak();
        Actor actor = cache.pop();
        if (cache.isEmpty() && !primaryActors.contains(actor))
            abort();
        else {
            actor.onUndo(step, true);
            boolean abortIfEmpty = true;
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
                    } else {
                        cache.pop();
                        abortIfEmpty = false;
                        actor.onUndo(step, false);
                        break;
                    }
                }

            } else if (!primaryActors.contains(actor)) {
                Actor original = actor;
                boolean isPrimary;
                do {
                    step = cache.peak();
                    if (step == null) break;
                    actor = cache.getActor(step);
                    isPrimary = primaryActors.contains(actor);
                    if (!isPrimary) cache.pop();
                    else cache.reset(step);
                    actor.onUndo(step, !isPrimary);
                } while (!isPrimary);
            }


            if (cache.isEmpty() && abortIfEmpty) abort();
            else throw e;
        }
    }

    private void notifyActorsOfComplete(R result) {

        for (PrimaryActor actor : primaryActors)
            //noinspection SuspiciousMethodCalls
            if (preconditionActor != actor && !resultActors.contains(actor) && actor != null)
                //noinspection unchecked
                actor.onComplete(event);

        for (SecondaryActor actor : secondaryActors)
            //noinspection SuspiciousMethodCalls
            if (preconditionActor != actor && !resultActors.contains(actor) && actor != null)
                //noinspection unchecked
                actor.onComplete(event);

        if (containerResultActor != null)
            containerResultActor.onComplete(event, result);
    }

    private void notifyActorsOfAbort(TectonicEvent event) {
        for (Actor actor : primaryActors)
            if (preconditionActor != actor && actor != null) //noinspection unchecked
                actor.onAbort(event);

        for (Actor actor : secondaryActors)
            if (preconditionActor != actor && actor != null) //noinspection unchecked
                actor.onAbort(event);

        if (containerResultActor != null)
            containerResultActor.onAbort(event);
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

    public static void clearAll() {

        for (UseCase useCase : ALIVE.values())
            useCase.cache.clear();

        ALIVE.clear();
    }

    /**
     *
     */
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
        public <D> D waitFor(@Nonnull Actor actor, UUID key) throws ExecutionException, UndoException, InterruptedException {
            return UseCase.this.waitFor(actor, new IsolatedStep(), key);
        }

        @Override
        public <D> D waitFor(@Nonnull Actor actor, Step step, UUID key) throws ExecutionException, UndoException, InterruptedException {
            return UseCase.this.waitFor(actor, step, key);
        }

        @Override
        public <D> D waitForSafe(@Nonnull Actor actor, UUID key) throws UndoException, InterruptedException {
            return UseCase.this.waitForSafe(actor, new IsolatedStep(), key);
        }

        @Override
        public <D> D waitForSafe(@Nonnull Actor actor, Step step, UUID key) throws UndoException, InterruptedException {
            return UseCase.this.waitForSafe(actor, step, key);
        }

        @Override
        public <D> D waitFor(@Nonnull Actor actor, UUID key, Runnable runnable) throws InterruptedException, ExecutionException, UndoException {
            return UseCase.this.waitFor(actor, new IsolatedStep(), key, runnable);
        }

        @Override
        public <D> D waitFor(@Nonnull Actor actor, Step step, UUID key, Runnable runnable) throws InterruptedException, ExecutionException, UndoException {
            return UseCase.this.waitFor(actor, step, key, runnable);
        }

        @SafeVarargs
        @Override
        public final <D> D waitFor(@Nonnull Actor actor, UUID key, Class<? extends Exception>... exs) throws UnexpectedStep, InterruptedException {
            return UseCase.this.waitFor(actor, new IsolatedStep(), key, exs);
        }

        @SafeVarargs
        @Override
        public final <D> D waitFor(@Nonnull Actor actor, Step step, UUID key, Class<? extends Exception>... exs) throws UnexpectedStep, InterruptedException {
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
        public void reset() {
            UseCase.this.reset();
        }
    }

    private class IsolatedStep implements Step {

        @Override
        public void terminate() {

        }
    }
}

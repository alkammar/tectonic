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

/**
 * <p>
 * - The whole idea of this framework is built on the assumption that business logic does not change frequently
 * in a mature industry. At least not as frequent as the IO logic, like UI designs, backend services ... etc.
 * This where the name "Tectonic" comes from, since the earth tectonic plates are moving but relatively
 * very slow, allowing us to build our civilization on top of it which on the other hand is developing
 * way faster than the tectonic plates are shifting. This understanding lead to the creation of this
 * framework to try to provide a good way of separation without compromising on interaction.
 * </p>
 * <p>
 * - The most elegant way of defining business logic (in my opinion) is (and always was) use cases.
 * Currently not used as much in application design, this framework aims to bring back use case design
 * in applications by mirroring the use case in code.
 * </p>
 * <p>
 * - The use case abstraction aims to provide a way to mirror a use cases in code, providing means to
 * define essential parts of it like triggers, primary actors, secondary actors, preconditions, main
 * scenario and alternate scenarios. It is a different perspective of clean architecture, with a main
 * goal to encapsulate business flow irrespective of the platform. Achieving that will allow shipping
 * the use cases as separate module(s) that can preserve business logic from IO changes (e.g. UI, backend,
 * storage ... etc).
 * </p>
 * <p>
 * - A more ambitious goal is to reuse the use case logic across different applications and clients
 * who operate within the same industry.
 * </p>
 * <p>
 * - To use the use case abstraction, there are a set of rules it is built on that you need to know
 * first to understand its behaviors and limitations.
 * </p>
 * <p>
 * - A use case is an orchestrator of actions/steps from Actors that needs to be executed in a specific
 * sequence (the main scenario) to fulfill the use case functionality. Actors can reply to the requested
 * actions with Exceptions (alternate/error scenarios) that should be handled by the use case implementation
 * to steer them in the desired direction (e.g. by requesting the UI actor to show an error message
 * and give it a chance to try again).
 * </p>
 * <p>
 * - An actor is a component of the use case logic that is essential to complete its functionality.
 * The use case requests an action from the actors and the actors can reply immediately or block
 * the use case thread to send a delayed reply. Actors will mainly implement IO operations like UI
 * (e.g. showing a screen or waiting for a user action), backend integration (REST APIs), local
 * file system or database, sensors ... etc. The typical way to define actors is to define them as
 * inner interfaces in the use case class.
 * </p>
 * <p>
 * - A use case interacts with a set of actors defined in its implementation. Actor can be one of
 * 3 kinds. {@link PrimaryActor}, {@link SecondaryActor>} or undefined. Primary and secondary actors
 * will receive the callbacks {@link Actor#onStart(Object, UseCaseHandle)}, {@link Actor#onUndo(Step, boolean)},
 * {@link Actor#onComplete(Object)} and {@link Actor#onAbort(Object)}, undefined actors will receive
 * none. Primary actor can {@link UseCaseHandle#undo()} and {@link UseCaseHandle#abort()} a use case.
 * Secondary actor can only {@link UseCaseHandle#undo()}. To add a primary actor to the use case
 * the actor must implement {@link PrimaryActor} and be added to the set passed by {@link #onAddPrimaryActors(Set)}
 * override. To add a secondary actor to the use case the actor must implement {@link SecondaryActor}
 * and be added to the set passed by {@link #onAddSecondaryActors(Set)} override. Nothing special
 * needs to be done for undefined actors (that is why they are undefined).
 * </p>
 * <p>
 * - A use case can have a dynamic number of added actors observing its state and result. These actors are
 * called {@link ResultActor}s. They can be added at creation time through {@link Builder#resultActor(ResultActor[])}.
 * Result actors can receive {@link ResultActor#onComplete(Object, Object)} and {@link ResultActor#onAbort(Object)}
 * </p>
 * <p>
 * - A use case can have another use case defined as its precondition by overriding {@link #onAddPreconditions(Set)}
 * and add the precondition use case class(es) to the passed set. Preconditions will be executed
 * before the use case starts its execution and the use case will be blocked until all the preconditions
 * are completed.
 * </p>
 * <p>
 * - Currently the framework supports one use case instance at a time. So if you implemented
 * use case A and use case B, you can execute one instance of A and one instance of B simultaneously,
 * but you cannot execute multiple instances of A or B at the same time. If you wish to re-execute
 * a use case you have to wait for the current instance to terminate to be able to execute another
 * instance.
 * </p>
 * <p>
 * - Each use case instance runs in its own worker thread, where the life of the thread is managed
 * by a {@link ThreadManager}. The use case has a default thread manager implementation {@link ThreadManagerImpl}
 * which should not be replaced in production. But a way to replace the thread manager is provided
 * to replace the default behavior for testing purposes through {@link #setGlobalThreadManager(ThreadManager)}
 * and {@link #setThreadManager(ThreadManager)}.
 * </p>
 * <p>
 * - A use case scenario is meant to be executed in sequence. In other words the use case abstraction
 * is not designed to execute steps in parallel. Although this looks like a limitation, it is not.
 * It is a fundamental rule of the abstraction to imitate use case design. With that said, you
 * still can run parallel code inside the use case, but at that point from a design perspective,
 * it is deviating from being a use case and you need to handle that on your own.
 * </p>
 * <p>
 * - The use case body will typically request a sequence of actions from actors defined by the
 * use case. These actions in a normal environment will mostly be asynchronous in nature. The
 * asynchronous/synchronous nature of the actors actions is left to the actor's methods implementation.
 * So the use case abstraction provides a set of tools to synchronize asynchronous the actors
 * calls to be able to achieve the previous point. The synchronization is the responsibility
 * of the actors and not the use case. Typically the actors should call a pair of {@link UseCaseHandle#waitFor(Actor, Step, UUID)}
 * (or one of its derivatives) and {@link UseCaseHandle#replyWith(UUID)} (or one of its derivatives).
 * The {@code waitFor} will cause the use case thread to block until it is unblocked by the
 * call to {@code replyWith}. This will achieve the sense of a use case executing in sequence
 * although calling asynchronous actor's methods.
 * </p>
 * <p>
 * - The use case holds a stack of executed {@link Step}s. A step is defined by a call to one
 * of {@link UseCaseHandle#waitFor(Actor, Step, UUID)}. Each time an actor blocks a use case
 * with this method the use case adds the step to the top of the stack. This is used to provide
 * the ability for actors to undo their steps.
 * </p>
 * <p>
 * - The abstraction supports the undo feature. Think of a UI actor that displays multiple screens
 * to the user as part of its flow, and the user wants to go back in the UI flow. Or a REST API
 * call that triggered a challenge action that the user decided to cancel that challenge for
 * any reason. The undo feature allows the use case to clear its top step of the stack to allow
 * the actor to retry the action again. Undo is explained in details in {@link UseCaseHandle#undo()}.
 * </p>
 * <p>
 * - A use case can be executed by calling one of the {@link #execute()} method versions.
 * A Use cases can be executed a sub use case for a container use case using any of the versions
 * of {@link #execute(Class)}.
 * </p>
 * <p>
 * - For the use case to terminate it must end with one of either {@link #complete()}, {@link #complete(Object)},
 * {@link #abort()}, {@link #retry(boolean)} or {@link #retry()}. The calls to these methods
 * should be inside the {@link #onExecute()} override which you use to implement the use case
 * main body of execution and its alternate/error scenarios. Failure to do so the use case thread
 * will stay alive and will result in some problems like the inability to re-execute the use
 * case again. Alternatively actors can decide to terminate a use case prematurely through {@link UseCaseHandle#abort()}.
 * Other ways for the use case to terminate is when another use case's completion or abortion is
 * configured to complete or abort the current use case through the overriding of {@link #completeWhenCompleted(Set)},
 * {@link #completeWhenAborted(Set)}, {@link #abortWhenCompleted(Set)} and {@link #abortWhenAborted(Set)} (Set)}.
 * </p>
 * <p>
 * - To create/build a use case instance it is recommended to use {@link Builder} class, which
 * provides a set of methods to configure the use case. Alternatively you can use {@link #fetch(Class)}
 * plus any combination of the methods used by {@link Builder#build()} implementation.
 * </p>
 * <p>
 * - For setting the use case dependencies (e.g. actors), override the {@link #onInitialize()}
 * method if you are using dependency injection. If you are not using dependency injection get
 * a hold on the use case instance before execution either at creation time or by calling {@link #fetch(Class)}
 * and set your dependencies on that instance.
 * </p>
 * <p>
 * - A use case can return a result object. This is useful for example for sub use cases. To
 * return a result set the result type in the {@link R} generic parameter and call the method
 * {@link #complete(Object)} at the end of the use case flow. The result will be delivered to
 * any {@link ResultActor} observing this use case via {@link ResultActor#onComplete(Object, Object)}
 * and will be returned by a call to {@link #execute(UUID, Class)} for executing a sub use case.
 * </p>
 *
 * @param <R> the use case result type
 */
@SuppressWarnings({"WeakerAccess", "unused"})
@SuppressLint("UseSparseArrays")
public abstract class UseCase<R> {

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

    private static final Map<String, UseCase> ALIVE = new ConcurrentHashMap<>();
    public static final UUID PRECONDITION_KEY = UUID.randomUUID();

    private Triggers<?> executor;
    private Thread thread;
    private StepCache cache = new StepCache();

    private Synchronizer<?> blockingSynchronizer;
    private boolean running;

    private ThreadManager threadManager;
    private ThreadManager defaultThreadManager = new ThreadManagerImpl();

    private Set<PrimaryActor> primaryActors = new LinkedHashSet<>();
    private Set<SecondaryActor> secondaryActors = new LinkedHashSet<>();
    private Set<ResultActor<TectonicEvent, R>> resultActors = new HashSet<>();
    private SecondaryActor preconditionActor;

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
    private String instanceId;

    /**
     * Returns the current (only) instance that is alive of this {@code useCaseClass}. If no instance
     * is running it will create one and returns it. If an exception is thrown during the creation of
     * the use case a {@link UnableToInstantiateUseCase} runtime exception is thrown wrapping the
     * original exception.
     *
     * @param useCaseClass the use case class to fetch
     * @param <U>          the use case type
     * @return the use case instance
     */
    public synchronized static <U extends UseCase> U fetch(Class<U> useCaseClass) {
        return fetch(useCaseClass, "");
    }

    public synchronized static <U extends UseCase> U fetch(Class<U> useCaseClass, @Nonnull String instanceId) {

        U useCase;
        synchronized (ALIVE) {
            //noinspection unchecked
            useCase = (U) ALIVE.get(useCaseClass.getName() + instanceId);
            if (useCase == null) {
                try {
                    useCase = useCaseClass.newInstance();
                    useCase.onCreate();
                    ALIVE.put(useCase.getId(), useCase);
                } catch (Exception e) {
                    e.printStackTrace();
                    throw new UnableToInstantiateUseCase(e.getCause());
                }
            }
        }

        return useCase;
    }

    String getId() {
        return getClass().getName() + instanceId;
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

        if (ALIVE.containsKey(getId()) && !running) {

            running = true;

            getThreadManager().start(new ThreadManager.UseCaseExecution() {

                boolean initialized = false;

                @Override
                public void run() throws InterruptedException, UndoException {

                    if (!initialized) {
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
                        if (!executePreconditions()) {
                            abort();
                        } else {
                            if (executeOnStart) notifyActorsOfStart(event);
                            initialized = true;
                        }
                    }

                    if (!aborted && initialized) {
                        try {
                            onExecute();
                        } catch (UndoException e) {
                            if (running) notifyActorsOfUndo(e);
                        }
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
                public void onComplete() {
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
     * Called before {@link #onExecute()} once in the life of the use case. It provides a good spot
     * to initialize the use case (e.g. injecting dependencies);
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
     * @throws UndoException        thrown if the sub use case was aborted
     * @throws InterruptedException if the use case thread was interrupted while waiting for the
     *                              sub use case to finish
     */
    protected <r> r execute(final UUID key, final Class<? extends UseCase<r>> cls) throws UndoException, InterruptedException {

        if (cache.contains(cls)) {
            return cache.getValue(cls);
        } else {
            return executeSubUseCase(key, cls);
        }
    }

    private <r> r executeSubUseCase(UUID key, Class<? extends UseCase<r>> cls) throws UndoException, InterruptedException {

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

        //noinspection unchecked
        Triggers<TectonicEvent> triggers = (Triggers<TectonicEvent>) executor;
        TectonicEvent subEvent = triggers.map(cls, event);

        final UseCase<?> useCase = new Builder()
                .useCase(cls)
                .containerResultActor(subResultActor)
                .container(this)
                .triggers(triggers)
                .build();

        //noinspection unchecked
        useCase.addResultActor((ResultActor) triggers.observe(event, subEvent, useCase));

        PrimaryActor subPrimaryActor = new PrimaryActor() {
            @Override
            public void onStart(Object event, UseCaseHandle handle) {

            }

            @Override
            public void onUndo(Step step, boolean inclusive) {
                useCase.cache.pop();
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

        useCase.execute(subEvent);

        try {
            r result = waitFor(subPrimaryActor, ANONYMOUS_STEP, finalKey);
            cache.put(cls, ANONYMOUS_STEP, finalKey);
            return result;
        } catch (ExecutionException e) {
            if (e.getCause() instanceof UndoException) throw (UndoException) e.getCause();
        }

        return null;
    }

    private boolean executePreconditions() throws UndoException {

        if (!preconditionsExecuted) onAddPreconditions(preconditions);

        SecondaryActor<TectonicEvent> preconditionActor = new SecondaryActor<TectonicEvent>() {

            @Override
            public void onStart(TectonicEvent event, UseCaseHandle handle) {

            }

            @Override
            public void onUndo(Step step, boolean inclusive) {

            }

            @Override
            public void onComplete(TectonicEvent event) {
                preconditions.remove(preconditionEvents.get(event));
                preconditionEvents.remove(event);
                if (preconditions.isEmpty()) replyWith(PRECONDITION_KEY);
            }

            @Override
            public void onAbort(TectonicEvent event) {
//                abort();
                preconditions.clear();
                preconditionEvents.clear();
                replyWith(PRECONDITION_KEY, new InterruptedException());
            }
        };

        //noinspection unchecked
        Triggers<TectonicEvent> triggers = (Triggers<TectonicEvent>) executor;
        for (Class<? extends UseCase> precondition : preconditions) {

            TectonicEvent preconditionEvent = triggers.map(precondition, event);

            preconditionEvents.put(preconditionEvent, precondition);

            final UseCase<?> useCase = new Builder()
                    .useCase(precondition)
                    .preconditionActor(preconditionActor)
                    .triggers(triggers)
                    .build();

            //noinspection unchecked
            useCase.addResultActor((ResultActor) triggers.observe(event, preconditionEvent, useCase));

            useCase.execute(preconditionEvent);
        }

        if (preconditions.size() > 0) {
            try {
                waitFor(ANONYMOUS_ACTOR, ANONYMOUS_STEP, PRECONDITION_KEY);
                return true;
            } catch (ExecutionException | InterruptedException e) {
                return false;
            }
        } else {
            return true;
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

    /**
     * For testing purposes only. Sets this use case's instance custom thread manager. Pass null to clear.
     *
     * @param threadManager thread manager implementation
     */
    protected void setThreadManager(ThreadManager threadManager) {
        this.threadManager = threadManager;
    }

    /**
     * For testing purposes only. Sets a global custom thread manager for all use cases. Pass null to clear.
     *
     * @param threadManager thread manager implementation
     */
    public static void setGlobalThreadManager(ThreadManager threadManager) {
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
     * <p>
     * Called when the use case starts to execute. You should write here all your use case business
     * logic, including the main scenario and the alternate scenarios. This can be executed multiple
     * times based on your implementation choices for the actors (e.g. using {@link UseCaseHandle#replyWithRandom(UUID, Random)})
     * or the undo scenarios. Multiple execution should be handled by the actors since the use case will
     * be caching all its steps objects returned by any of {@link #waitFor(Actor, Step, UUID)} derivatives.
     * For example for a UI actor an action implementation can create a new screen, you will need to
     * make sure that that screen is not created multiple times. The execution of the use case can throw
     * {@link InterruptedException} which is going to be caught by the use case {@link ThreadManager}.
     * It can also throw {@link UndoException} which can be thrown by any undo scenario.
     * </p>
     * <p>
     * A recommended implementation is to write the main scenario in a try block and other alternate
     * scenarios in the catch blocks. For example
     *
     * <p> try { </p>
     * <ul>
     * <p>      actor1.action1(); </p>
     * <p>      actor1.action2(); </p>
     * <p>      actor2.action1(); </p>
     * <p>      actor1.action3(); </p>
     * <p>      complete(); </p>
     * </ul>
     * <p> } catch (Exception a11) { </p>
     * <ul>
     * <p>      actor1.handleError(); </p>
     * <p>      retry(); </p>
     * </ul>
     * <p> } catch (Exception a21) { </p>
     * <ul>
     * <p>      actor1.handleAnotherError(); </p>
     * <p>      retry(); </p>
     * </ul>
     * <p> } catch (Exception a13) { </p>
     * <ul>
     * <p>      abort(); </p>
     * </ul>
     * <p> } </p>
     * </p>
     *
     * @throws InterruptedException thrown if the use case thread is interrupted
     * @throws UndoException        thrown when an actor requests to undo the top step
     */
    protected abstract void onExecute() throws InterruptedException, UndoException;

    private <D> D immediate(Actor actor, Step step, UUID key, D data) {
        return cache.push(actor, step, key, data);
    }

    private <D> D immediate(Actor actor, Step step, UUID key) {
        return immediate(actor, step, key, null);
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

    public void setPreconditionActor(SecondaryActor preconditionActor) {
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
     * Version of {@link #retry(boolean)} that does not cause the precondition use cases to be re-executed
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
     *                              in a call to {@link #onExecute()}
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
     * @see PrimaryActor
     * @see SecondaryActor
     * @see ResultActor
     */
    protected void complete(R result) {

        if (container != null && ALIVE.containsKey(container.getId())) {
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
            ALIVE.remove(getId());
            getThreadManager().stop();

            completeWhenCompleted(this);
            abortWhenCompleted(this);
        }
    }

    /**
     * Causes the use case to abort prematurely terminating its thread and triggering the {@link Actor#onAbort(Object)}
     * callback for primary and secondary actors and {@link ResultActor#onAbort(Object)} for result actors.
     */
    protected void abort() {

        aborted = true;

        synchronized (ALIVE) {
            onDestroy();
            ALIVE.remove(getId());
        }
        getThreadManager().stop();
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
            if (primaryActors.contains(actor)) {
                Actor original = actor;
                step = cache.peak();
                while (step != null) {
                    actor = cache.getActor(step);
                    if (!primaryActors.contains(actor)) {
                        cache.pop();
                        actor.onUndo(step, true);
                        step = cache.peak();
                    } else {
                        cache.reset(step);
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


            if (cache.isEmpty()) abort();
            else throw e;
        }
    }

    private void notifyActorsOfComplete(R result) {

        for (PrimaryActor actor : primaryActors)
            //noinspection SuspiciousMethodCalls
            if (actor != null)
                //noinspection unchecked
                actor.onComplete(event);

        for (SecondaryActor actor : secondaryActors)
            //noinspection SuspiciousMethodCalls
            if (actor != null)
                //noinspection unchecked
                actor.onComplete(event);

        if (containerResultActor != null)
            containerResultActor.onComplete(event, result);
    }

    private void notifyActorsOfAbort(TectonicEvent event) {
        for (Actor actor : primaryActors)
            //noinspection SuspiciousMethodCalls
            if (actor != null && !resultActors.contains(actor) && preconditionActor != actor) //noinspection unchecked
                actor.onAbort(event);

        for (Actor actor : secondaryActors)
            //noinspection SuspiciousMethodCalls
            if (actor != null && !resultActors.contains(actor) && preconditionActor != actor) //noinspection unchecked
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
     * Called after the use case is either completed or aborted to provide a chance for cleanup code
     * to be executed.
     */
    protected void onDestroy() {

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
        public <D> D immediate(@Nonnull Actor actor, UUID key) {
            return UseCase.this.immediate(actor, new IsolatedStep(), key);
        }

        @Override
        public <D> D immediate(@Nonnull Actor actor, UUID key, D data) {
            return UseCase.this.immediate(actor, new IsolatedStep(), key, data);
        }

        @Override
        public <D> D immediate(@Nonnull Actor actor, Step step, UUID key) {
            return UseCase.this.immediate(actor, step, key);
        }

        @Override
        public <D> D immediate(@Nonnull Actor actor, Step step, UUID key, D data) {
            return UseCase.this.immediate(actor, step, key, data);
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

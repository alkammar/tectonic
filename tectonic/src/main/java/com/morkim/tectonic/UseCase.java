package com.morkim.tectonic;


import android.os.Looper;
import android.util.SparseArray;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import io.reactivex.Observable;
import io.reactivex.ObservableEmitter;
import io.reactivex.ObservableOnSubscribe;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.annotations.NonNull;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Consumer;
import io.reactivex.schedulers.Schedulers;
import io.reactivex.subjects.ReplaySubject;
import io.reactivex.subjects.Subject;


@SuppressWarnings({"WeakerAccess", "unused"})
public abstract class UseCase<Rq extends Request, Rs extends Result> {

    /**
     * Executes the use case in cached mode, where if a cached result exists it is returned with
     * executing the use case otherwise if a result does not exist the use case will be executed
     */
    public static final int CACHED = 0x01000000;

    /**
     * Executes the use case synchronously, so the execution is performed on the calling thread
     */
    public static final int EXECUTE_ON_MAIN = 0x10000000;

    public static final int UNDO = 0x00100000;

    public static final LooperConfigs STUB_LOOPER_CHECKER = new LooperConfigs() {

        @Override
        public boolean isMain() {
            return true;
        }

        @Override
        public boolean isSingleThread() {
            return true;
        }
    };

    static final int NO_ACTOR = 0;

    private static final int NO_FLAGS = 0x00000000;

    private static final LooperConfigs DEFAULT_LOOPER_CHECKER = new LooperConfigs() {

        @Override
        public boolean isMain() {
            return Looper.getMainLooper().getThread() == Thread.currentThread();
        }

        @Override
        public boolean isSingleThread() {
            return false;
        }
    };

    static LooperConfigs looperConfigs = DEFAULT_LOOPER_CHECKER;

    private enum Type {
        START,
        UPDATE,
        COMPLETE,
        ACTION,
        ERROR,
        UNDO,
    }

    private Rq request;
    private int flags;

    private StateMachine stateMachine = new StateMachine();
    private Disposable subscription;

    private static Map<Class<? extends UseCase>, UseCase> running = new HashMap<>();

    private final List<Prerequisite> prerequisites;
    private int prerequisiteIndex;

    private static Map<Class<? extends UseCase>, Subscriptions> subscriptionMap = new HashMap<>();

    private Subject<UseCaseListener<? extends Result>> subscribers = ReplaySubject.create();

    private static Map<Class<? extends UseCase>, SparseArray<Result>> cachedResults = new HashMap<>();

    /**
     * This will fetch the use case if it is already running, otherwise this will create
     * a new instance of the use case
     *
     * @param useCaseClass The use case class that needs to be fetched
     * @param <U>          Use case type class
     * @return The fetched use case
     */
    public synchronized static <U extends UseCase> U fetch(Class<U> useCaseClass) {

        //noinspection unchecked
        U useCase = (U) running.get(useCaseClass);
        if (useCase == null) {
            try {
                useCase = useCaseClass.newInstance();
                useCase.onAddPrerequisites();
                running.put(useCaseClass, useCase);
                useCase.create();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        return useCase;
    }

    void create() {
        stateMachine.create();
    }

    protected UseCase() {

        if (subscriptionMap.get(this.getClass()) == null)
            subscriptionMap.put(this.getClass(), new Subscriptions());

        prerequisites = new ArrayList<>();
    }

    public void undo() {
        execute(UNDO);
    }

    /**
     * Executes this use case instance if it is only executable
     */
    public void execute() {
        execute(null, 0);
    }

    public void execute(int flags) {
        execute(null, flags);
    }

    /**
     * Executes this use case instance if it is only executable
     *
     * @param request The use case request
     */
    public void execute(final Rq request) {
        execute(request, 0);
    }

    /**
     * Executes this use case instance if it is only executable
     *
     * @param request The use case request
     * @param flags   Execution flags
     */
    public void execute(Rq request, int flags) {

        if (isExecuteCached(flags))
            executeCached(request, flags);
        else if (stateMachine.isExecutable())
            executeAsync(request, flags);
    }

    private boolean isExecuteCached(int flags) {
        return (flags & CACHED) == CACHED;
    }

    private void executeCached(final Rq request, final int flags) {

        if (supportsCaching()) {

            if (cachedResults.get(UseCase.this.getClass()) == null)
                cachedResults.put(UseCase.this.getClass(), new SparseArray<Result>());

            final Rs result = findCachedResult(request);

            subscription = Observable.create(new ObservableOnSubscribe<Event>() {
                @Override
                public void subscribe(@NonNull ObservableEmitter<Event> e) throws Exception {

                    if (isCachedExecutable(result)) {
                        UseCase.this.notify(new Event(Type.START));
                        UseCase.this.notify(new Event(Type.UPDATE, result));
                        UseCase.this.notify(new Event(Type.COMPLETE));
                    } else {
                        execute(request, flags & EXECUTE_ON_MAIN);
                    }

                }
            }).observeOn(AndroidSchedulers.mainThread())
                    .subscribeOn(isExecuteOnMain(flags) ? AndroidSchedulers.mainThread() : Schedulers.io())
                    .subscribe();
        } else
            execute(request);
    }

    private Rs findCachedResult(Rq request) {
        //noinspection unchecked
        return (Rs) cachedResults.get(UseCase.this.getClass())
                .get(request == null ? Request.NO_ID : request.id());
    }

    private boolean isExecuteOnMain(int flags) {
        return (flags & EXECUTE_ON_MAIN) == EXECUTE_ON_MAIN;
    }

    private void executeAsync(Rq request, int flags) {

        this.request = request;
        this.flags = flags;

        subscription = Observable.create(new ObservableOnSubscribe<Event>() {
            @Override
            public void subscribe(@NonNull ObservableEmitter<Event> e) throws Exception {

                UseCase.this.notify(new Event(Type.START));

                if (!stateMachine.isInProgress()) {
                    stateMachine.start();

                    if (!prerequisites.isEmpty()) {
                        prerequisiteIndex = 0;
                        executePrerequisite();
                    } else {
                        if ((UseCase.this.flags & UNDO) == UNDO) {

                            final Rs result = findCachedResult(UseCase.this.request);

                            if (result != null) {
                                onUndo(UseCase.this.request, result);
                            }

                            if (!stateMachine.isDead()) {
                                stateMachine.kill();
                                running.remove(UseCase.this.getClass());
                                UseCase.this.flags = NO_FLAGS;
                            }

                        } else
                            onExecute(UseCase.this.request);
                    }
                }

            }
        }).observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(isExecuteOnMain(flags) ? AndroidSchedulers.mainThread() : Schedulers.io())
                .subscribe(
                        new Consumer<Event>() {
                            @Override
                            public void accept(@NonNull Event event) throws Exception {

                            }
                        },
                        new Consumer<Throwable>() {
                            @Override
                            public void accept(@NonNull Throwable throwable) throws Exception {

                                stateMachine.kill();
                                running.remove(UseCase.this.getClass());
                                UseCase.this.flags = NO_FLAGS;

                                UseCase.this.notify(new Event(Type.ERROR, throwable));
                            }
                        });
    }

    protected void onAddPrerequisites() {

    }

    protected abstract void onExecute(Rq request) throws Exception;

    protected void onUndo(Rq request, Rs oldResult) throws Exception {

    }

    protected void addPrerequisite(Class<? extends UseCase> useCase) {
        addPrerequisite(Precondition.TRUE_PRECONDITION, useCase, null);
    }

    protected void addPrerequisite(Class<? extends UseCase> useCase, UseCaseListener listener) {
        addPrerequisite(Precondition.TRUE_PRECONDITION, useCase, listener);
    }

    protected void addPrerequisite(Precondition precondition, Class<? extends UseCase> useCase) {
        addPrerequisite(precondition, useCase, null);
    }

    protected void addPrerequisite(Precondition precondition, Class<? extends UseCase> useCase, UseCaseListener listener) {
        prerequisites.add(new Prerequisite(useCase, precondition, listener));
    }

    private void executePrerequisite() throws Exception {

        final Prerequisite prerequisite = prerequisites.get(prerequisiteIndex);

        if (prerequisite.precondition.onEvaluate()) {

            final UseCase prerequisiteUseCase = UseCase.fetch(prerequisite.useCase);

            if (prerequisite.listener != null)
                //noinspection unchecked
                prerequisiteUseCase.subscribe(prerequisite.listener);

            //noinspection unchecked
            subscribe(prerequisite.useCase,
                    new SimpleDisposableUseCaseListener<Result>() {
                        @Override
                        public void onComplete() {
                            tryNext();
                        }

                        @Override
                        public boolean onError(Throwable throwable) {
                            tryNext();

                            return false;
                        }

                        private void tryNext() {
                            try {
                                executeNextPrerequisite();
                            } catch (Exception e) {

                                stateMachine.kill();
                                running.remove(UseCase.this.getClass());
                                flags = NO_FLAGS;

                                UseCase.this.notify(new Event(Type.ERROR, e));
                            }
                        }
                    }
            );
            prerequisiteUseCase.execute();
        } else {
            executeNextPrerequisite();
        }
    }

    private void executeNextPrerequisite() throws Exception {

        prerequisiteIndex++;
        if (prerequisiteIndex < prerequisites.size()) {
            executePrerequisite();
        } else {
            onExecute(request);
        }
    }

    private boolean isCachedExecutable(Rs result) {
        return stateMachine.isCachedExecutable() &&
                result != null;
    }

    private void notify(@NonNull final Event event) {
        switch (event.type) {
            case START:
                subscriptionMap.get(this.getClass()).notifyStart();
                break;
            case UPDATE:
                updateCache(event.result);
                subscriptionMap.get(this.getClass()).notifyUpdate(event.result);
                break;
            case COMPLETE:
                subscriptionMap.get(this.getClass()).notifyComplete();
                break;
            case ACTION:
                subscriptionMap.get(this.getClass()).notifyActionRequired(event.actor, event.codes);
                break;
            case ERROR:
                subscriptionMap.get(this.getClass()).notifyError(event.error);
                break;
            case UNDO:
                subscriptionMap.get(this.getClass()).notifyUndone(event.result);
                clearCache(this.getClass());
                break;
        }
    }

    private void updateCache(Result result) {
        SparseArray<Result> resultsMap;
        if (cachedResults.get(UseCase.this.getClass()) == null) {
            resultsMap = new SparseArray<>();
            cachedResults.put(UseCase.this.getClass(), resultsMap);
        } else
            resultsMap = cachedResults.get(UseCase.this.getClass());

        resultsMap.put(request == null ? Request.NO_ID : request.id(), result);
    }

    protected boolean supportsCaching() {
        return true;
    }

    /**
     * Subscribes a {@link UseCaseListener} to this use case. The subscribed listener will stay
     * subscribed to the use case even after the use case has completed.
     * The subscriber will have to be un-subscribed in order not receive further updates.
     *
     * @param useCaseListener Subscriber listener
     * @return The subscribed use case
     */
    public UseCase<Rq, Rs> subscribe(UseCaseListener<Rs> useCaseListener) {
        subscribe(this.getClass(), useCaseListener);

        return this;
    }

    /**
     * Subscribes a {@link UseCaseListener} to this use case. All subscribers will get all callbacks
     * except {@link UseCaseListener#onActionRequired(List)}, this callback will only be received by
     * the appropriate actor.
     * The subscribed listener will stay subscribed to the use case even after the use case has completed.
     * The subscriber will have to be un-subscribed in order not receive further updates.
     *
     * @param actor Actor to receive a required action request
     * @param useCaseListener Subscriber listener
     * @return The subscribed use case
     */
    public UseCase<Rq, Rs> subscribe(int actor, UseCaseListener<Rs> useCaseListener) {
        subscribe(this.getClass(), actor, useCaseListener);

        return this;
    }

    /**
     * Notifies of a use case update to all subscribers
     *
     * @param result Update result
     */
    protected void notifySubscribers(Rs result) {
        if (!stateMachine.isDead())
            notify(new Event(Type.UPDATE, result));
    }

    /**
     * Broadcasts a use case error to all subscribers. This to be mainly used in async calls from
     * with in a use case
     *
     * @param throwable Update error
     */
    protected void notifySubscribers(Throwable throwable) {
        if (!stateMachine.isDead()) {

            stateMachine.kill();
            running.remove(UseCase.this.getClass());
            UseCase.this.flags = NO_FLAGS;

            notify(new Event(Type.ERROR, throwable));
        }
    }

    /**
     * Un-subscribes a use listener from this use case instance
     *
     * @param useCaseListener Un-subscriber listener
     */
    public void unsubscribe(UseCaseListener<Rs> useCaseListener) {
        unsubscribe(this.getClass(), useCaseListener);
    }

    /**
     * Clears cached results for this use case
     *
     * @param useCaseClass The use case to its clear cache
     */
    public static void clearCache(Class<? extends UseCase> useCaseClass) {
        cachedResults.remove(useCaseClass);
    }

    /**
     * Subscribes a {@link UseCaseListener} to this use case. The subscribed listener will stay
     * subscribed to the use case even after the use case has completed.
     * The subscriber will have to be un-subscribed in order not receive further updates.
     *
     * @param useCaseClass The use case to subscribe to
     * @param listener Subscriber listener
     */
    public static void subscribe(Class<? extends UseCase> useCaseClass, UseCaseListener<? extends Result> listener) {
        subscribe(useCaseClass, NO_ACTOR, listener);
    }

    /**
     * Subscribes a {@link UseCaseListener} to this use case. All subscribers will get all callbacks
     * except {@link UseCaseListener#onActionRequired(List)}, this callback will only be received by
     * the appropriate actor.
     * The subscribed listener will stay subscribed to the use case even after the use case has completed.
     * The subscriber will have to be un-subscribed in order not receive further updates.
     *
     * @param useCaseClass The use case to subscribe to
     * @param actor Actor to receive a required action request
     * @param listener Subscriber listener
     */
    public static void subscribe(Class<? extends UseCase> useCaseClass, int actor, UseCaseListener<? extends Result> listener) {

        if (subscriptionMap == null) subscriptionMap = new HashMap<>();

        Subscriptions subscriptions = subscriptionMap.get(useCaseClass);
        if (subscriptions == null) {
            subscriptions = new Subscriptions();
            subscriptionMap.put(useCaseClass, subscriptions);
        }

        subscriptions.remove(listener);
        subscriptions.add(actor, listener);
    }

    public static void unsubscribe(Class<? extends UseCase> useCaseClass, UseCaseListener<? extends Result> listener) {

        Subscriptions subscriptions = subscriptionMap.get(useCaseClass);
        if (subscriptions != null) {
            if (listener != null)
                subscriptions.remove(listener);
            else
                subscriptions.clear();
        }
    }

    public static void unsubscribe(Class<? extends UseCase> useCaseClass) {
        unsubscribe(useCaseClass, null);
    }

    public static void unsubscribeAll() {
        for (Class<? extends UseCase> useCaseClass : subscriptionMap.keySet())
            UseCase.unsubscribe(useCaseClass, null);
        subscriptionMap.clear();
    }

    public static void clearAllInProgress() {
        for (UseCase useCase : running.values()) {
            useCase.stateMachine.kill();
            useCase.flags = NO_FLAGS;
        }
        running.clear();
    }

    /**
     * Marks the use case as completed, changing its state to DEAD and broadcasting
     * the {@link com.morkim.tectonic.UseCaseListener#onComplete} callback to subscribers.
     */
    protected void finish() {

        if (!stateMachine.isDead()) {
            stateMachine.finish();
            running.remove(UseCase.this.getClass());
            if ((flags & UNDO) == UNDO)
                notify(new Event(Type.UNDO, findCachedResult(null)));
            else
                notify(new Event(Type.COMPLETE));

            flags = NO_FLAGS;

            onPostExecute();
        }
    }

    public static void cancel(Class<? extends UseCase> useCaseClass) {

        UseCase useCase = running.get(useCaseClass);
        if (useCase != null)
            useCase.cancel();
    }

    protected void cancel() {

        if (subscription != null) subscription.dispose();

        subscriptionMap.get(this.getClass()).notifyCancel();

        stateMachine.kill();
        running.remove(this.getClass());
        flags = NO_FLAGS;
    }

    protected void requestAction(int actor, Integer... codes) {

        stateMachine.askForInput();
        notify(new Event(Type.ACTION, actor, codes));
    }

    protected RequiredInputs startInputValidation() {
        return new RequiredInputs(this);
    }

    protected void onPostExecute() {

    }

    class Event {

        Type type;
        Rs result;
        int actor;
        Integer[] codes;
        Throwable error;

        public Event(Type type) {
            this(type, (Rs) null);
        }

        Event(Type type, Rs result) {
            this.type = type;
            this.result = result;
        }

        Event(Type type, int actor, Integer[] codes) {
            this.type = type;
            this.actor = actor;
            this.codes = codes;
        }

        public Event(Type type, Throwable throwable) {
            this.type = type;
            this.error = throwable;
        }
    }

    public static void setLooperConfigs(LooperConfigs looperConfigs) {
        UseCase.looperConfigs = looperConfigs;
    }
}

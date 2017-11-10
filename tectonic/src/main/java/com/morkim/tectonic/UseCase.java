package com.morkim.tectonic;


import android.util.SparseArray;

import java.util.ArrayList;
import java.util.Arrays;
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

@SuppressWarnings({"WeakerAccess", "unused"})
public abstract class UseCase<Rq extends Request, Rs extends Result> {

    private enum State {
        NOT_CREATED,
        CREATED,
        IN_PROGRESS,
        DEAD,
    }

    private enum Type {
        START,
        UPDATE,
        COMPLETE,
        INPUT,
    }

    private Rq request;

    private ObservableEmitter<Event> observer;
    private State state = State.NOT_CREATED;
    private Disposable subscription;

    private static Map<Class<? extends UseCase>, UseCase> running = new HashMap<>();

    private final List<Prerequisite> prerequisites;
    private int prerequisiteIndex;

    private static Map<Class<? extends UseCase>, List<UseCaseListener<? extends Result>>> subscriptions = new HashMap<>();
    private static Map<Class<? extends UseCase>, List<UseCaseListener<? extends Result>>> consumedStarts = new HashMap<>();

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
        state = State.CREATED;
    }

    protected UseCase() {

        if (subscriptions.get(this.getClass()) == null)
            subscriptions.put(this.getClass(), new ArrayList<UseCaseListener<? extends Result>>());

        if (consumedStarts.get(this.getClass()) == null)
            consumedStarts.put(this.getClass(), new ArrayList<UseCaseListener<? extends Result>>());

        prerequisites = new ArrayList<>();
    }

    public void execute() {
        execute(null);
    }

    private class Event {

        Type type;
        Rs result;
        Integer[] codes;

        Event(Type type) {
            this(type, (Rs) null);
        }

        Event(Type type, Rs result) {
            this.type = type;
            this.result = result;
        }

        Event(Type type, Integer[] codes) {
            this.type = type;
            this.codes = codes;
        }
    }

    /**
     * Executes this use case instance if it is only executable
     *
     * @param request The use case request
     */
    public void execute(final Rq request) {

        if (!isExecutable()) return;

        this.request = request;

        subscription = Observable.create(new ObservableOnSubscribe<Event>() {
            @Override
            public void subscribe(@NonNull ObservableEmitter<Event> e) throws Exception {

                UseCase.this.observer = e;
                executeOnObservable();

            }
        }).observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.io())
                .subscribe(onNext);
    }

    private boolean isExecutable() {
        return state == State.CREATED || state == State.IN_PROGRESS;
    }

    private void executeOnObservable() {

        observer.onNext(new Event(Type.START));

        if (state != State.IN_PROGRESS) {
            state = State.IN_PROGRESS;

            if (!prerequisites.isEmpty()) {
                prerequisiteIndex = 0;
                executePrerequisite();
            } else {
                onExecute(UseCase.this.request);
            }
        }
    }

    protected void onAddPrerequisites() {

    }

    protected abstract void onExecute(Rq request);

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

    private void executePrerequisite() {

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
                            subscription = Observable.create(new ObservableOnSubscribe<Event>() {
                                @Override
                                public void subscribe(@NonNull ObservableEmitter<Event> e) throws Exception {
                                    executeNextPrerequisite();
                                }
                            }).observeOn(AndroidSchedulers.mainThread())
                                    .subscribeOn(Schedulers.io())
                                    .subscribe();
                        }
                    });
            prerequisiteUseCase.execute();
        } else {
            executeNextPrerequisite();
        }
    }

    private void executeNextPrerequisite() {

        prerequisiteIndex++;
        if (prerequisiteIndex < prerequisites.size()) {
            executePrerequisite();
        } else {
            onExecute(request);
        }
    }

    /**
     * Executes the use case in cached mode, where if a cached result exists it is returned with
     * executing the use case otherwise if a result does not exist the use case will be executed
     */
    public void executeCached() {
        executeCached(null);
    }

    public void executeCached(final Rq request) {

        if (supportsCaching()) {
            subscription = Observable.create(new ObservableOnSubscribe<Event>() {
                @Override
                public void subscribe(@NonNull ObservableEmitter<Event> e) throws Exception {

                    UseCase.this.observer = e;

                    if (cachedResults.get(UseCase.this.getClass()) == null)
                        cachedResults.put(UseCase.this.getClass(), new SparseArray<Result>());
                    //noinspection unchecked
                    final Rs result = (Rs) cachedResults.get(UseCase.this.getClass())
                            .get(request == null ? Request.NO_ID : request.id());
                    if (isCachedExecutable(result)) {
                        e.onNext(new Event(Type.START));
                        e.onNext(new Event(Type.UPDATE, result));
                        e.onNext(new Event(Type.COMPLETE));
                    } else
                        execute(request);
                }
            }).observeOn(AndroidSchedulers.mainThread())
                    .subscribeOn(Schedulers.io())
                    .subscribe(onNext);
        } else
            execute(request);
    }

    private boolean isCachedExecutable(Rs result) {
        return (state == State.DEAD || state == State.CREATED) &&
                result != null;
    }

    private Consumer<Event> onNext = new Consumer<Event>() {
        @Override
        public void accept(@NonNull Event event) throws Exception {

            switch (event.type) {
                case START:
                    for (UseCaseListener listener : subscriptions.get(UseCase.this.getClass())) {
                        List<UseCaseListener<? extends Result>> consumedListener = consumedStarts.get(UseCase.this.getClass());
                        if (!consumedListener.contains(listener))
                            listener.onStart();

                        if (!consumedListener.contains(listener))
                            //noinspection unchecked
                            consumedListener.add(listener);
                    }
                    break;
                case UPDATE:
                    for (UseCaseListener listener : subscriptions.get(UseCase.this.getClass()))
                        //noinspection unchecked
                        listener.onUpdate(event.result);

                    SparseArray<Result> resultsMap;
                    if (cachedResults.get(UseCase.this.getClass()) == null) {
                        resultsMap = new SparseArray<>();
                        cachedResults.put(UseCase.this.getClass(), resultsMap);
                    } else
                        resultsMap = cachedResults.get(UseCase.this.getClass());

                    resultsMap.put(request == null ? Request.NO_ID : request.id(), event.result);
                    break;
                case COMPLETE:
                    List<UseCaseListener<? extends Result>> useCaseSubscriptions = subscriptions.get(UseCase.this.getClass());
                    for (int i = 0; i < useCaseSubscriptions.size(); i++) {
                        UseCaseListener listener = useCaseSubscriptions.get(i);
                        listener.onComplete();

                        if (listener instanceof DisposableUseCaseListener) {
                            useCaseSubscriptions.remove(listener);
                            i--;
                        }
                        consumedStarts.get(UseCase.this.getClass()).remove(listener);
                    }
                    break;
                case INPUT:
                    List<UseCaseListener<? extends Result>> useCaseListeners = subscriptions.get(UseCase.this.getClass());
                    for (int i = useCaseListeners.size() - 1; i >= 0; i--) {
                        if (useCaseListeners.get(i).onInputRequired(Arrays.asList(event.codes)))
                            break;
                    }
                    break;
            }
        }
    };

    protected boolean supportsCaching() {
        return false;
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
     * Broadcasts a use case update to all subscribers
     *
     * @param result Update result
     */
    protected void updateSubscribers(Rs result) {
        if (state != State.DEAD)
            observer.onNext(new Event(Type.UPDATE, result));
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

    public static void subscribe(Class<? extends UseCase> useCaseClass, UseCaseListener<? extends Result> listener) {

        if (subscriptions == null) subscriptions = new HashMap<>();

        List<UseCaseListener<? extends Result>> useCaseListeners = subscriptions.get(useCaseClass);
        if (useCaseListeners == null) {
            useCaseListeners = new ArrayList<>();
            subscriptions.put(useCaseClass, useCaseListeners);
        }

        useCaseListeners.remove(listener);
        useCaseListeners.add(listener);
    }

    public static void unsubscribe(Class<? extends UseCase> useCaseClass, UseCaseListener<? extends Result> listener) {

        List<UseCaseListener<? extends Result>> useCaseListeners = subscriptions.get(useCaseClass);
        if (useCaseListeners != null) {
            if (listener != null)
                useCaseListeners.remove(listener);
            else
                useCaseListeners.clear();
        }
    }

    public static void unsubscribe(Class<? extends UseCase> useCaseClass) {
        unsubscribe(useCaseClass, null);
    }

    public static void unsubscribeAll() {
        subscriptions.clear();
    }

    public static void clearAllInProgress() {
        for (UseCase useCase : running.values())
            useCase.state = State.DEAD;
        running.clear();
    }

    /**
     * Marks the use case as completed, changing its state to DEAD and broadcasting
     * the {@link com.morkim.tectonic.UseCaseListener#onComplete} callback to subscribers.
     */
    protected void finish() {

        if (state != State.DEAD) {
            state = State.DEAD;
            running.remove(UseCase.this.getClass());
            observer.onNext(new Event(Type.COMPLETE));
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

        for (UseCaseListener listener : subscriptions.get(this.getClass())) {
            consumedStarts.get(UseCase.this.getClass()).remove(listener);
            listener.onCancel();
        }

        state = State.DEAD;
        running.remove(this.getClass());
    }

    protected void requestInput(Integer... codes) {

        state = State.CREATED;
        observer.onNext(new Event(Type.INPUT, codes));
    }

    protected RequiredInputs startInputValidation() {
        return new RequiredInputs(this);
    }

    protected void onPostExecute() {

    }
}

package com.morkim.tectonic;


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
import io.reactivex.functions.Consumer;
import io.reactivex.schedulers.Schedulers;

import static com.morkim.tectonic.UseCase.Type.INPUT;
import static com.morkim.tectonic.UseCase.Type.UPDATE;

@SuppressWarnings({"WeakerAccess", "unused"})
public abstract class UseCase<Rq extends Request, Rs extends Result> {

    private Rq request;

    private ObservableEmitter<Update> observer;
    protected State state = State.NOT_CREATED;

    private class Prerequisite {

        Class<? extends UseCase> useCase;
        UseCaseListener listener;
        boolean condition;

        Prerequisite(Class<? extends UseCase> useCase, boolean condition, UseCaseListener listener) {

            this.useCase = useCase;
            this.condition = condition;
            this.listener = listener;
        }
    }

    private static Map<Class<? extends UseCase>, UseCase> running = new HashMap<>();

    private final List<Prerequisite> prerequisites;
    private int prerequisiteIndex;

    private static Map<Class<? extends UseCase>, List<UseCaseListener<? extends Result>>> subscriptions = new HashMap<>();
    private static Map<Class<? extends UseCase>, List<UseCaseListener<? extends Result>>> consumedStarts = new HashMap<>();

    private static Map<Class<? extends UseCase>, SparseArray<Result>> cachedResults = new HashMap<>();

    private enum State {
        NOT_CREATED,
        CREATED,
        IN_PROGRESS,
        DEAD,
    }

    /**
     * This will fetch the use case if it is already running, otherwise this will create
     * a new instance of the use case
     *
     * @param useCaseClass The use case class that needs to be fetched
     * @param <U>          Use case type
     * @return The fetched use case
     */
    public static <U extends UseCase> U fetch(Class<U> useCaseClass) {

        //noinspection unchecked
        U useCase = (U) running.get(useCaseClass);
        if (useCase == null) {
            try {
                useCase = useCaseClass.newInstance();
                running.put(useCaseClass, useCase);
                useCase.state = State.CREATED;
            } catch (InstantiationException e) {
                e.printStackTrace();
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            }
        }

        return useCase;
    }

    protected UseCase() {

        if (subscriptions.get(this.getClass()) == null)
            subscriptions.put(this.getClass(), new ArrayList<UseCaseListener<? extends Result>>());

        if (consumedStarts.get(this.getClass()) == null)
            consumedStarts.put(this.getClass(), new ArrayList<UseCaseListener<? extends Result>>());

        prerequisites = new ArrayList<>();
        onAddPrerequisites();
    }

    public void execute() {
        execute(null);
    }

    enum Type {
        START,
        UPDATE,
        COMPLETE,
        INPUT,
    }

    private class Update {

        Type type;
        Rs result;
        int code;

        Update(Type type) {
            this(type, null);
        }

        Update(Type type, Rs result) {
            this.type = type;
            this.result = result;
        }

        Update(Type type, int code) {
            this.type = type;
            this.code = code;
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

//        if (observer == null) {
        Observable.create(new ObservableOnSubscribe<Update>() {
            @Override
            public void subscribe(@NonNull ObservableEmitter<Update> e) throws Exception {

                UseCase.this.observer = e;
                executeOnObservable();

            }
        }).observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.io())
                .subscribe(onNext);
//        }
//        else {
//            observer
//            executeOnObservable();
//        }
    }

    private boolean isExecutable() {
        return state == State.CREATED || state == State.IN_PROGRESS;
    }

    private void executeOnObservable() {

        observer.onNext(new Update(Type.START));

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
        addPrerequisite(useCase, true, null);
    }

    protected void addPrerequisite(Class<? extends UseCase> useCase, UseCaseListener listener) {
        addPrerequisite(useCase, true, listener);
    }

    protected void addPrerequisite(Class<? extends UseCase> useCase, boolean condition) {
        addPrerequisite(useCase, condition, null);
    }

    protected void addPrerequisite(Class<? extends UseCase> useCase, boolean condition, UseCaseListener listener) {
        prerequisites.add(new Prerequisite(useCase, condition, listener));
    }

    private void executePrerequisite() {

        final Prerequisite prerequisite = prerequisites.get(prerequisiteIndex);

        if (prerequisite.condition) {
            final UseCase prerequisiteUseCase = UseCase.fetch(prerequisite.useCase);
            if (prerequisite.listener != null)
                //noinspection unchecked
                prerequisiteUseCase.subscribe(prerequisite.listener);
            //noinspection unchecked
            subscribe(prerequisite.useCase,
                    new SimpleDisposableUseCaseListener<Result>() {
                        @Override
                        public void onComplete() {
                            Observable.create(new ObservableOnSubscribe<Update>() {
                                @Override
                                public void subscribe(@NonNull ObservableEmitter<Update> e) throws Exception {
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
            Observable.create(new ObservableOnSubscribe<Update>() {
                @Override
                public void subscribe(@NonNull ObservableEmitter<Update> e) throws Exception {

                    UseCase.this.observer = e;

                    if (cachedResults.get(UseCase.this.getClass()) == null)
                        cachedResults.put(UseCase.this.getClass(), new SparseArray<Result>());
                    //noinspection unchecked
                    Rs result = (Rs) cachedResults.get(UseCase.this.getClass())
                            .get(request == null ? Request.NO_ID : request.id());
                    if (result != null)
                        updateSubscribers(result);
                    else
                        execute(request);

                }
            }).observeOn(AndroidSchedulers.mainThread())
                    .subscribeOn(Schedulers.io())
                    .subscribe(onNext);
        } else
            execute(request);
    }

    private Consumer<Update> onNext = new Consumer<Update>() {
        @Override
        public void accept(@NonNull Update update) throws Exception {

            switch (update.type) {
                case START:
                    for (UseCaseListener listener : subscriptions.get(UseCase.this.getClass())) {
                        if (!consumedStarts.get(UseCase.this.getClass()).contains(listener))
                            listener.onStart();
                        consumedStarts.get(UseCase.this.getClass()).add(listener);
                    }
                    break;
                case UPDATE:
                    for (UseCaseListener listener : subscriptions.get(UseCase.this.getClass()))
                        //noinspection unchecked
                        listener.onUpdate(update.result);

                    SparseArray<Result> resultsMap;
                    if (cachedResults.get(UseCase.this.getClass()) == null) {
                        resultsMap = new SparseArray<>();
                        cachedResults.put(UseCase.this.getClass(), resultsMap);
                    } else
                        resultsMap = cachedResults.get(UseCase.this.getClass());

                    resultsMap.put(request == null ? Request.NO_ID : request.id(), update.result);
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
                    for (UseCaseListener listener : subscriptions.get(UseCase.this.getClass()))
                        listener.onInputRequired(update.code);
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
        observer.onNext(new Update(UPDATE, result));
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

        if (subscriptions.get(useCaseClass) == null)
            subscriptions.put(useCaseClass, new ArrayList<UseCaseListener<? extends Result>>());
        subscriptions.get(useCaseClass).add(listener);
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

        state = State.DEAD;
        running.remove(UseCase.this.getClass());
        observer.onNext(new Update(Type.COMPLETE));
        onPostExecute();
    }

    public static void cancel(Class<? extends UseCase> useCaseClass) {

        UseCase useCase = running.get(useCaseClass);
        if (useCase != null)
            useCase.cancel();
    }

    protected void cancel() {

        for (UseCaseListener listener : subscriptions.get(this.getClass()))
            listener.onCancel();

        state = State.DEAD;
        running.remove(this.getClass());
    }

    protected void requestInput(int code) {

        state = State.CREATED;
        observer.onNext(new Update(INPUT, code));
    }

    protected void onPostExecute() {

    }
}

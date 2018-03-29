package com.morkim.tectonic;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executors;

import io.reactivex.Scheduler;
import io.reactivex.annotations.NonNull;
import io.reactivex.functions.Consumer;
import io.reactivex.schedulers.Schedulers;

class Subscriptions {

    private Map<UseCaseListener, Subscription> subscriptionMap = new LinkedHashMap<>();
    private List<Subscription> subscriptionList = new ArrayList<>();
    private List<Subscription> consumedStartList = new ArrayList<>();
    private Map<Integer, Subscription> actorMap = new LinkedHashMap<>();

    private Scheduler scheduler = UseCase.looperConfigs.isSingleThread() ?
            Schedulers.trampoline() :
            Schedulers.from(Executors.newSingleThreadExecutor());
    private boolean errorHandled;

    void add(int actor, UseCaseListener<? extends Result> listener) {
        Subscription subscription = new Subscription<>(scheduler, listener);

        subscriptionList.add(subscription);
        synchronized (this) {
            subscriptionMap.put(listener, subscription);
        }

        actorMap.put(actor, subscription);
    }

    void remove(UseCaseListener listener) {
        subscriptionList.remove(subscriptionMap.get(listener));
        synchronized (this) {
            subscriptionMap.remove(listener);
        }
    }

    private void remove(Subscription subscription) {
        subscriptionList.remove(subscription);
        synchronized (this) {
            subscriptionMap.remove(subscription.getListener());
        }
    }

    void clear() {
        subscriptionList.clear();
        synchronized (this) {
            subscriptionMap.clear();
        }
    }

    void notifyStart() {

        synchronized (this) {
            for (final Subscription subscription : subscriptionMap.values())
                subscription.dispatch(new Consumer<Subscription>() {
                    @Override
                    public void accept(@NonNull Subscription s) throws Exception {
                        if (!consumedStartList.contains(s)) {
                            s.getListener().onStart();
                            consumedStartList.add(s);
                        }
                    }
                });
        }
    }

    <Rs extends Result> void notifyUpdate(final Rs result) {

        synchronized (this) {
            for (final Subscription subscription : subscriptionMap.values())
                subscription.dispatch(new Consumer<Subscription>() {
                    @Override
                    public void accept(@NonNull Subscription s) throws Exception {
                        //noinspection unchecked
                        s.getListener().onUpdate(result);
                    }
                });
        }
    }

    void notifyComplete() {

        final Collection<Subscription> subscriptions = new ArrayList<>();
        synchronized (this) {
            subscriptions.addAll(subscriptionMap.values());
        }

        for (final Subscription subscription : subscriptions)
            subscription.dispatch(new Consumer<Subscription>() {
                @Override
                public void accept(@NonNull Subscription s) throws Exception {

                    consumedStartList.remove(s);
                    s.getListener().onComplete();
//                Log.i("Thread", subscription + ": " + Thread.currentThread().getName());

                    removeIfDisposable(subscription);
                }
            });
    }

    void notifyCancel() {

        synchronized (this) {
            for (final Subscription subscription : subscriptionMap.values()) {
                subscription.dispatch(new Consumer<Subscription>() {
                    @Override
                    public void accept(@NonNull Subscription s) throws Exception {
                        consumedStartList.remove(s);
                        s.getListener().onCancel();

//                    removeIfDisposable(subscription);
                    }
                });
            }
        }
    }

    void notifyActionRequired(int actor, Integer[] codes) {
        if (actorMap.containsKey(actor))
            actorMap.get(actor).getListener().onActionRequired(Arrays.asList(codes));
    }

    void notifyError(final Throwable throwable) {

        errorHandled = false;

        if (subscriptionList.size() > 0) {
            notifyError(throwable, subscriptionList.size() - 1);
        }
    }

    private void notifyError(final Throwable throwable, int index) {

        subscriptionList.get(index)
                .dispatch(new Consumer<Subscription>() {
                    @Override
                    public void accept(@NonNull Subscription s) throws Exception {

                        if (!errorHandled && s.getListener().onError(throwable)) {
                            errorHandled = true;
                        }

                        if (s.equals(subscriptionList.get(0)) && !errorHandled) {
                            throw new Error(throwable);
                        } else if (subscriptionList.indexOf(s) - 1 >= 0) {
                            notifyError(throwable, subscriptionList.indexOf(s) - 1);
                        } else
                            errorHandled = false;

                        removeIfDisposable(s);
                    }
                });
    }

    private void removeIfDisposable(Subscription subscription) {
        if (subscription.getListener() instanceof DisposableUseCaseListener) {
            subscription.detach();
            remove(subscription);
        }
    }

    <Rs extends Result> void notifyUndone(final Rs result) {

        final Collection<Subscription> subscriptions = new ArrayList<>();
        synchronized (this) {
            subscriptions.addAll(subscriptionMap.values());
        }

        for (final Subscription subscription : subscriptions)
            subscription.dispatch(new Consumer<Subscription>() {
                @Override
                public void accept(@NonNull Subscription s) throws Exception {

                    consumedStartList.remove(s);
                    //noinspection unchecked
                    s.getListener().onUndone(result);

                    removeIfDisposable(subscription);
                }
            });
    }
}

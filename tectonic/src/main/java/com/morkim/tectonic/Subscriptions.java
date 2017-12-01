package com.morkim.tectonic;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import io.reactivex.annotations.NonNull;
import io.reactivex.functions.Consumer;

class Subscriptions {

    private Map<UseCaseListener, Subscription> subscriptionMap = new LinkedHashMap<>();
    private List<Subscription> subscriptionList = new ArrayList<>();
    private List<Subscription> consumedStartList = new ArrayList<>();

    void add(Subscription subscription) {
        subscriptionList.add(subscription);
        subscriptionMap.put(subscription.getListener(), subscription);
    }

    void remove(UseCaseListener listener) {
        subscriptionList.remove(subscriptionMap.get(listener));
        subscriptionMap.remove(listener);
    }

    private void remove(Subscription subscription) {
        subscriptionList.remove(subscription);
        subscriptionMap.remove(subscription.getListener());
    }

    void clear() {
        subscriptionMap.clear();
    }

    List<Subscription> getSubscriptions() {
        return subscriptionList;
    }

    void notifyStart() {

        for (final Subscription subscription : subscriptionMap.values())
            subscription.dispatch(new Consumer<Object>() {
                @Override
                public void accept(@NonNull Object o) throws Exception {
                    if (!consumedStartList.contains(subscription)) {
                        subscription.getListener().onStart();
                        consumedStartList.add(subscription);
                    }
                }
            });
    }

    <Rs extends Result> void notifyUpdate(final Rs result) {

        for (final Subscription subscription : subscriptionMap.values())
            subscription.dispatch(new Consumer<Object>() {
                @Override
                public void accept(@NonNull Object o) throws Exception {
                    //noinspection unchecked
                    subscription.getListener().onUpdate(result);
                }
            });
    }

    void notifyComplete() {

        final Collection<Subscription> subscriptions = new ArrayList<>();
        subscriptions.addAll(subscriptionMap.values());

        for (final Subscription subscription : subscriptions)
            subscription.dispatch(new Consumer<Object>() {
                @Override
                public void accept(@NonNull Object o) throws Exception {

                    consumedStartList.remove(subscription);
                    subscription.getListener().onComplete();
//                Log.i("Thread", subscription + ": " + Thread.currentThread().getName());

                    if (subscription.getListener() instanceof DisposableUseCaseListener) {
                        subscription.detach();
                        remove(subscription);
                    }
                }
            });
    }

    void notifyCancel() {

        for (final Subscription subscription : subscriptionMap.values()) {
            subscription.dispatch(new Consumer<Object>() {
                @Override
                public void accept(@NonNull Object o) throws Exception {
                    consumedStartList.remove(subscription);
                    subscription.getListener().onCancel();
                }
            });

        }
    }

    void notifyInputRequired(Integer[] codes) {

        for (int i = subscriptionList.size() - 1; i >= 0; i--) {
            if (subscriptionList.get(i).getListener().onInputRequired(Arrays.asList(codes)))
                break;
        }
    }
}

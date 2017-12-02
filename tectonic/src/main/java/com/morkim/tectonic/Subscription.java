package com.morkim.tectonic;

import io.reactivex.Observable;
import io.reactivex.ObservableEmitter;
import io.reactivex.ObservableOnSubscribe;
import io.reactivex.Scheduler;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.annotations.NonNull;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Consumer;
import io.reactivex.schedulers.Schedulers;

class Subscription<R extends Result> {
    
    private UseCaseListener<R> listener;
    private Disposable disposable;
    private Observable<Subscription<R>> observable;
    private boolean isMain = true;

    Subscription(Scheduler scheduler, UseCaseListener<R> listener) {

        this.listener = listener;

        isMain = UseCase.looperConfigs.isMain();

        observable = Observable.create(new ObservableOnSubscribe<Subscription<R>>() {
            @Override
            public void subscribe(@NonNull ObservableEmitter<Subscription<R>> e) throws Exception {
                e.onNext(Subscription.this);
            }
        }).subscribeOn(scheduler);
    }

    void dispatch(Consumer<Subscription> onNext) {
        synchronized (this) {
            disposable = observable
                    .observeOn(isMain ? AndroidSchedulers.mainThread() : Schedulers.io())
                    .subscribe(onNext);
        }
    }

    void detach() {
        synchronized (this) {
            if (disposable != null) disposable.dispose();
        }
    }

    UseCaseListener<R> getListener() {
        return listener;
    }
}

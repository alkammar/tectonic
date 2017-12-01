package com.morkim.tectonic;

import io.reactivex.Observable;
import io.reactivex.ObservableEmitter;
import io.reactivex.ObservableOnSubscribe;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.annotations.NonNull;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Consumer;
import io.reactivex.schedulers.Schedulers;

class Subscription<R extends Result> {
    
    private UseCaseListener<R> listener;
    private Disposable disposable;
    private Observable<Object> observable;
    private boolean isMain = true;

    Subscription(UseCaseListener<R> listener) {
        attach(listener);
    }

    private void attach(final UseCaseListener<R> listener) {

        this.listener = listener;

        isMain = UseCase.onCheckLooper.isMain();

        observable = Observable.create(new ObservableOnSubscribe<Object>() {
            @Override
            public void subscribe(@NonNull ObservableEmitter<Object> e) throws Exception {
                e.onNext(new Object());
            }
        }).subscribeOn(Schedulers.io());
    }

    void dispatch(Consumer<Object> onNext) {
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

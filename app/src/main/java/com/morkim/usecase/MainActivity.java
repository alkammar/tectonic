package com.morkim.usecase;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.TextView;

import com.morkim.tectonic.UseCase;
import com.morkim.tectonic.UseCaseListener;

import io.reactivex.Observable;
import io.reactivex.Observer;
import io.reactivex.annotations.NonNull;
import io.reactivex.disposables.Disposable;

public class MainActivity extends AppCompatActivity {

    private final ReactiveUseCase reactiveUseCase = new ReactiveUseCase();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        {
            Observer<String> observer = new Observer<String>() {
                @Override
                public void onSubscribe(@NonNull Disposable d) {
                    ((TextView) findViewById(R.id.label)).append("\nonSubscribe");
                }

                @Override
                public void onNext(@NonNull String s) {
                    ((TextView) findViewById(R.id.label)).append("\nonNext");
                }

                @Override
                public void onError(@NonNull Throwable e) {

                }

                @Override
                public void onComplete() {

                }
            };

            Observer<String> observer2 = new Observer<String>() {
                @Override
                public void onSubscribe(@NonNull Disposable d) {
                    ((TextView) findViewById(R.id.label)).append("\nonSubscribe2");
                }

                @Override
                public void onNext(@NonNull String s) {
                    ((TextView) findViewById(R.id.label)).append("\nonNext2");
                }

                @Override
                public void onError(@NonNull Throwable e) {

                }

                @Override
                public void onComplete() {

                }
            };

            Observable<String> observable = new Observable<String>() {
                @Override
                protected void subscribeActual(Observer<? super String> observer) {
                    observer.onNext("ihif");
                }
            };

//            findViewById(R.id.button).setOnClickListener(v -> {
//                observable
//                        .subscribeOn(Schedulers.io())
//                        .observeOn(AndroidSchedulers.mainThread())
//                        .subscribe(observer);
//
//                observable
//                        .subscribeOn(Schedulers.io())
//                        .observeOn(AndroidSchedulers.mainThread())
//                        .subscribe(observer2);
//            });
        }

        /////////////////////////////////////////////////////////////////
        {
//            ConnectableObservable<UseCaseListener<ReactiveResult>> connectableObservable =
//                    new ConnectableObservable<UseCaseListener<ReactiveResult>>() {
//                        @Override
//                        public void connect(@NonNull Consumer<? super Disposable> connection) {
//
//                        }
//
//                        @Override
//                        protected void subscribeActual(Observer<? super UseCaseListener<ReactiveResult>> observer) {
//                            observer.onNext(new UseCaseListener<ReactiveResult>() {
//                                @Override
//                                public void onStart() {
//
//                                }
//
//                                @Override
//                                public void onUpdate(ReactiveResult result) {
//                                    ((TextView) findViewById(R.id.label)).append("\nonUpdate");
//                                }
//
//                                @Override
//                                public void onComplete() {
//
//                                }
//
//                                @Override
//                                public void onCancel() {
//
//                                }
//
//                                @Override
//                                public void onInputRequired(int code) {
//
//                                }
//                            });
//                        }
//                    }
//                    .subscribeOn(Schedulers.io())
//                    .observeOn(AndroidSchedulers.mainThread())
//                    .publish();
//
//            findViewById(R.id.button).setOnClickListener(v -> {
////                connectableObservable.subscribe(ucl -> ((TextView) findViewById(R.id.label)).append("\n" + ucl + "1"));
////                connectableObservable.subscribe(ucl -> ((TextView) findViewById(R.id.label)).append("\n" + ucl + "2"));
//                connectableObservable.subscribe(new Observer<UseCaseListener<ReactiveResult>>() {
//                    @Override
//                    public void onSubscribe(@NonNull Disposable d) {
//
//                    }
//
//                    @Override
//                    public void onNext(@NonNull UseCaseListener<ReactiveResult> reactiveResultUseCaseListener) {
//                        reactiveResultUseCaseListener.onUpdate(null);
//                    }
//
//                    @Override
//                    public void onError(@NonNull Throwable e) {
//
//                    }
//
//                    @Override
//                    public void onComplete() {
//
//                    }
//                });
//
//                connectableObservable.subscribe(new Observer<UseCaseListener<ReactiveResult>>() {
//                    @Override
//                    public void onSubscribe(@NonNull Disposable d) {
//
//                    }
//
//                    @Override
//                    public void onNext(@NonNull UseCaseListener<ReactiveResult> reactiveResultUseCaseListener) {
//                        reactiveResultUseCaseListener.onUpdate(null);
//                    }
//
//                    @Override
//                    public void onError(@NonNull Throwable e) {
//
//                    }
//
//                    @Override
//                    public void onComplete() {
//
//                    }
//                });
//
//                connectableObservable.connect();
//            });
        }

        /////////////////////////////////////////////////////////////////

        findViewById(R.id.button).setOnClickListener(v -> reactiveUseCase
                .subscribe(new UseCaseListener<ReactiveResult>() {
                    @Override
                    public void onStart() {
                        ((TextView) findViewById(R.id.label)).append("\nonStart " + Thread.currentThread().getName());
                    }

                    @Override
                    public void onUpdate(ReactiveResult result) {
                        ((TextView) findViewById(R.id.label)).append("\nonUpdate " + Thread.currentThread().getName());
                    }

                    @Override
                    public void onComplete() {
                        ((TextView) findViewById(R.id.label)).append("\nonComplete " + Thread.currentThread().getName());
                    }

                    @Override
                    public void onCancel() {
                        ((TextView) findViewById(R.id.label)).append("\nonCancel " + Thread.currentThread().getName());
                    }

                    @Override
                    public void onInputRequired(int code) {
                        ((TextView) findViewById(R.id.label)).append("\nonInputRequired " + Thread.currentThread().getName());
                    }
                })
                .subscribe(new UseCaseListener<ReactiveResult>() {
                    @Override
                    public void onStart() {
                        ((TextView) findViewById(R.id.label)).append("\nonStart2 " + Thread.currentThread().getName());
                    }

                    @Override
                    public void onUpdate(ReactiveResult result) {
                        ((TextView) findViewById(R.id.label)).append("\nonUpdate2 " + Thread.currentThread().getName());
                    }

                    @Override
                    public void onComplete() {
                        ((TextView) findViewById(R.id.label)).append("\nonComplete2 " + Thread.currentThread().getName());
                    }

                    @Override
                    public void onCancel() {
                        ((TextView) findViewById(R.id.label)).append("\nonCancel2 " + Thread.currentThread().getName());
                    }

                    @Override
                    public void onInputRequired(int code) {
                        ((TextView) findViewById(R.id.label)).append("\nonInputRequired2 " + Thread.currentThread().getName());
                    }
                })
                .execute());

        UseCase.clearAllInProgress();
    }
}

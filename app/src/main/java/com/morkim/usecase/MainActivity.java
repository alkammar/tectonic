package com.morkim.usecase;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.TextView;

import com.morkim.tectonic.UseCase;
import com.morkim.tectonic.UseCaseListener;

public class MainActivity extends AppCompatActivity {

    private final ReactiveUseCase reactiveUseCase = new ReactiveUseCase();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        {

//            findViewById(R.id.button).setOnClickListener(v -> {
//                ReplaySubject<String> subject = ReplaySubject.create();
//
//                Disposable sub = subject.subscribe(s -> ((TextView) findViewById(R.id.label)).append("\nOnExecute 1 " + s));
//
//                subject.onNext("listener 1");
//                sub.dispose();
//
//                subject.subscribe(new Observer<String>() {
//                    @Override
//                    public void onSubscribe(@NonNull Disposable d) {
//                        ((TextView) findViewById(R.id.label)).append("\nonSubscribe 2");
//                    }
//
//                    @Override
//                    public void onNext(@NonNull String integer) {
//                        ((TextView) findViewById(R.id.label)).append("\nOnExecute 2 " + integer);
//                    }
//
//                    @Override
//                    public void onError(@NonNull Throwable e) {
//
//                    }
//
//                    @Override
//                    public void onComplete() {
//                        ((TextView) findViewById(R.id.label)).append("\nonComplete 2");
//                    }
//                });
//
//                subject.onNext("listener 2");
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
                        }).execute());

        UseCase.clearAllInProgress();
    }
}

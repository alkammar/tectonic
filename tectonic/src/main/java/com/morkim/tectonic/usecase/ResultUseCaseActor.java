package com.morkim.tectonic.usecase;

public class ResultUseCaseActor<E, R> implements ResultActor<E, R> {

    private Class<? extends UseCase<E, R>> cls;
    private int key;

    public ResultUseCaseActor(Class<? extends UseCase<E, R>> cls, int key) {
        this.cls = cls;
        this.key = key;
    }

    @Override
    public void onComplete(E e, R result) {
        UseCase.replyWith(key, result);
    }

    @Override
    public void onAbort(E e) {
        UseCase.replyWith(key, new AbortedUseCase(cls));
    }
}

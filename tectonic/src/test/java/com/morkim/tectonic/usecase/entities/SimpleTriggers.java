package com.morkim.tectonic.usecase.entities;

import com.morkim.tectonic.usecase.AbortedUseCase;
import com.morkim.tectonic.usecase.PreconditionActor;
import com.morkim.tectonic.usecase.PrimaryActor;
import com.morkim.tectonic.usecase.ResultActor;
import com.morkim.tectonic.usecase.Triggers;
import com.morkim.tectonic.usecase.UseCase;

public class SimpleTriggers implements Triggers<Integer> {
    @Override
    public Integer trigger(Integer event, PreconditionActor<Integer> preconditionActor, PrimaryActor<Integer, ?> primaryActor, ResultActor<Integer, ?> resultActor) {
        return null;
    }

    @Override
    public Integer trigger(Integer event, PrimaryActor<Integer, ?> primaryActor) {
        return trigger(event, null, primaryActor, null);
    }

    @Override
    public Integer trigger(Integer event, ResultActor<Integer, ?> resultActor) {
        return trigger(event, null, null, resultActor);
    }

    @Override
    public Integer trigger(Integer event, PreconditionActor<Integer> preconditionActor) {
        return trigger(event, preconditionActor, null, null);
    }

    @Override
    public Integer trigger(Integer event) {
        return trigger(event, null, null, null);
    }

    @Override
    public <R> R trigger(Class<? extends UseCase<Integer, R>> cls, Integer contextEvent) throws InterruptedException, AbortedUseCase {
        return null;
    }
}

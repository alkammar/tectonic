package com.morkim.tectonic.flow;

public interface StepFactory {

    <S> S create(Class<S> aClass);

    <S> S create(Class<S> aClass, String instanceId);

    <S> void onCreated(S step);

    <S> void onCreated(S step, S impl);
}

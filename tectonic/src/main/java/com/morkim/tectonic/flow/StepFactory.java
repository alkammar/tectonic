package com.morkim.tectonic.flow;

public interface StepFactory {

    <S> S create(Class<S> aClass);

    <S> void onCreated(S step);
}

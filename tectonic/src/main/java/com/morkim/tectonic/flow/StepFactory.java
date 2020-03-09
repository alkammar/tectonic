package com.morkim.tectonic.flow;

public interface StepFactory {

    @Deprecated
    <S> S create(Class<? extends Step> aClass);

    @Deprecated
    <S> S create(Class<? extends Step> aClass, String instanceId);

    <S extends Step> S bind(S step, Class<S> aClass);

    <S extends Step> S bind(S step, Class<S> aClass, String instanceId);
}

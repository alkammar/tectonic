package com.morkim.tectonic.flow;

public interface StepFactory {

    <S> S create(Class<? extends Step> aClass);

    <S> S create(Class<? extends Step> aClass, String instanceId);
}

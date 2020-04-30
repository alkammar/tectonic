package com.morkim.tectonic.flow;

@SuppressWarnings("unused")
public interface StepFactory {

    /**
     * Same as {@link #create(Class, String)} with no instance ID.
     */
    <S> S create(Class<? extends Step> aClass);

    /**
     * Creates a step.
     */
    <S> S create(Class<? extends Step> aClass, String instanceId);
}

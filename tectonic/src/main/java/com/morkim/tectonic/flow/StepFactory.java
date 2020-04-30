package com.morkim.tectonic.flow;

import com.morkim.tectonic.ui.UIStep;

@SuppressWarnings("unused")
public interface StepFactory {

    /**
     * Same as {@link #create(Class, String)} with no instance ID.
     */
    <S extends UIStep> S create(Class<? extends Step> aClass);

    /**
     * Creates a step.
     */
    <S extends UIStep> S create(Class<? extends Step> aClass, String instanceId);
}

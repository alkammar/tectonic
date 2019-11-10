package com.morkim.tectonic.ui;

import android.app.Activity;
import android.os.Bundle;

import com.morkim.tectonic.flow.Step;
import com.morkim.tectonic.flow.StepFactory;

@SuppressWarnings({"unused", "SameParameterValue"})
public abstract class AbstractUIStepFactory<A extends Activity>
        implements StepFactory {

    private CoreUIStepFactory<A> coreUIStepFactory;

    public AbstractUIStepFactory(CoreUIStepFactory<A> coreUIStepFactory) {
        this.coreUIStepFactory = coreUIStepFactory;
    }

    @Override
    public <S> S create(Class<? extends Step> aClass) {
        return create(aClass, "");
    }

    @Override
    public <S> void onCreated(S step) {
        coreUIStepFactory.onCreated(step);
    }

    @Override
    public <S> void onCreated(S step, S impl) {
        coreUIStepFactory.onCreated(step, impl);
    }

    protected <S> S createActivityBlocking(Class<?> cls) throws InterruptedException {
        return coreUIStepFactory.createActivityBlocking(cls);
    }

    protected synchronized <S> S createActivityBlocking(Class<?> cls, int flags) throws InterruptedException {
        return coreUIStepFactory.createActivityBlocking(cls, flags);
    }

    protected synchronized <S> S createActivityBlocking(Class<?> cls, int flags, Bundle data) throws InterruptedException {
        return coreUIStepFactory.createActivityBlocking(cls, flags, data);
    }

    protected void createActivity(Class<?> cls) {
        coreUIStepFactory.createActivity(cls);
    }

    protected void createActivity(Class<?> cls, int flags) {
        coreUIStepFactory.createActivity(cls, flags);
    }

    protected synchronized <S> S retrieveActivity(Class<?> cls) throws InterruptedException {
        return coreUIStepFactory.retrieveActivity(cls);
    }
}

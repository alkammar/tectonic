package com.morkim.tectonic.ui;

import android.app.Activity;
import android.os.Bundle;

import com.morkim.tectonic.flow.Step;
import com.morkim.tectonic.flow.StepFactory;

@SuppressWarnings({"unused", "SameParameterValue", "WeakerAccess"})
public abstract class AbstractUIStepFactory<A extends Activity> implements StepFactory {

    private CoreUIStepFactory<A> coreUIStepFactory;

    public AbstractUIStepFactory(CoreUIStepFactory<A> coreUIStepFactory) {
        this.coreUIStepFactory = coreUIStepFactory;
    }

    @Override
    public final <S> S create(Class<? extends Step> aClass) {
        return create(aClass, "");
    }

    @Override
    public final <S> S create(Class<? extends Step> aClass, String instanceId) {
        try {
            return onCreateStep(aClass, instanceId);
        } catch (InterruptedException e) {
            // TODO need to handle if activity creation was interrupted
            e.printStackTrace();
            return null;
        }
    }

    protected abstract <S> S onCreateStep(Class<? extends Step> aClass, String instanceId) throws InterruptedException;

    protected <S extends UIStep> S createActivityBlocking(Class<? extends Activity> cls) throws InterruptedException {
        return coreUIStepFactory.createActivityBlocking(cls);
    }

    protected synchronized <S extends UIStep> S createActivityBlocking(Class<? extends Activity> cls, int flags) throws InterruptedException {
        return coreUIStepFactory.createActivityBlocking(cls, flags);
    }

    protected synchronized <S extends UIStep> S createActivityBlocking(Class<? extends Activity> cls, int flags, Bundle data) throws InterruptedException {
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

    protected synchronized <S> S retrieveActivity(Class<?> cls, Bundle data) throws InterruptedException {
        return coreUIStepFactory.retrieveActivity(cls, data);
    }
}

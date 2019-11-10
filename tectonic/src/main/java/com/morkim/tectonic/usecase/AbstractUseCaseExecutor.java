package com.morkim.tectonic.usecase;

import android.annotation.SuppressLint;
import android.util.Log;

import com.morkim.tectonic.flow.Step;

import java.util.HashMap;
import java.util.Map;

@SuppressWarnings({"WeakerAccess", "unused"})
public abstract class AbstractUseCaseExecutor<E extends TectonicEvent>
        implements PrimaryActor<E>,
        Triggers<E>, ResultActor<E, Object> {

    @SuppressLint("UseSparseArrays")
    private Map<Integer, UseCaseHandle> handles = new HashMap<>();

    @Override
    public ResultActor<E, ?> observe(E contextEvent, E implicitEvent, UseCase<?> useCase) {

        onPrepare(implicitEvent, "", contextEvent);

        return this;
    }

    @Override
    public E trigger(E event) {
        return trigger(event, "");
    }

    @Override
    public E trigger(E event, String instanceId) {
        return trigger(event, instanceId, null, emptyEvent());
    }

    @Override
    public E trigger(E event, ResultActor resultActor) {
        return trigger(event, resultActor, emptyEvent());
    }

    @Override
    public E trigger(E event, ResultActor resultActor, E contextEvent) {
        return trigger(event, "", resultActor, contextEvent);
    }

    protected abstract void onPrepare(E event, String instanceId, E contextEvent);

    @Override
    public E trigger(E event, String instanceId, ResultActor resultActor, E contextEvent) {
        return event;
    }

    protected abstract void onTerminate(E event, String instanceId);

    protected void execute(
            Class<? extends UseCase> cls,
            E event,
            ResultActor resultActor,
            E contextEvent) {

        if (!UseCase.isAlive(cls.getName()))
            onPrepare(event, "", contextEvent);

        new Builder()
                .useCase(cls)
                .resultActor(resultActor, (ResultActor) this)
                .triggers(this)
                .build()
                .execute(event);
    }

    private void execute(
            @SuppressWarnings("SameParameterValue") Class<? extends UseCase> cls,
            String instanceId,
            E event,
            ResultActor resultActor,
            E contextEvent) {

        if (!UseCase.isAlive(cls.getName() + instanceId))
            onPrepare(event, instanceId, contextEvent);

        new Builder()
                .useCase(cls)
                .instanceId(instanceId)
                .resultActor(resultActor, (ResultActor) this)
                .triggers(this)
                .build()
                .execute(event);
    }

    @Override
    public void onStart(E event, UseCaseHandle handle) {

    }

    @Override
    public void onUndo(Step step, boolean inclusive) {

    }

    @Override
    public void onComplete(E event) {

        Log.d("UseCaseExecutor", "onComplete: " + event);

        handles.remove(eventValue(event));
    }

    @Override
    public void onComplete(E event, Object result) {

        onTerminate(event, eventInstanceId(event));
    }

    @Override
    public void onAbort(E event) {

        Log.d("UseCaseExecutor", "onAbort: " + event);

        handles.remove(eventValue(event));
        onTerminate(event, eventInstanceId(event));
    }

    protected abstract String eventInstanceId(E event);

    protected abstract Integer eventValue(E event);

    protected abstract E emptyEvent();

    protected Map<Integer, UseCaseHandle> getHandles() {
        return handles;
    }
}

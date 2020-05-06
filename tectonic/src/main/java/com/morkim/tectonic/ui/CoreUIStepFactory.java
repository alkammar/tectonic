package com.morkim.tectonic.ui;

import android.app.Activity;
import android.app.Application;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.morkim.tectonic.flow.Step;
import com.morkim.tectonic.flow.StepCoordinator;
import com.morkim.tectonic.flow.StepFactory;
import com.morkim.tectonic.flow.StepListener;

import java.util.HashMap;
import java.util.Map;

@SuppressWarnings({"WeakerAccess", "unused", "SameParameterValue"})
public class CoreUIStepFactory<A extends Activity>
        implements StepFactory, StepListener,
        Application.ActivityLifecycleCallbacks {

    public static final String NO_REPLY = "no.reply";
    public static final String ACTION_FETCH_REFERENCE = "action.fetch.reference";
    public static final String FRAGMENT = "fragment";

    private A topActivity;
    private Map<Class<?>, UIStep> stepsMap = new HashMap<>();

    private Application app;

    public CoreUIStepFactory(Application app) {
        this.app = app;
        app.registerActivityLifecycleCallbacks(this);
    }

    @Override
    public <S extends UIStep> S create(Class<? extends Step> aClass) {
        return create(aClass, "");
    }

    @Override
    public <S extends UIStep> S create(Class<? extends Step> aClass, String instanceId) {
        return null;
    }

    @Override
    public <S> void onCreated(S step) {
        onCreated(step, step);
    }

    @Override
    public <S> void onCreated(S step, S impl) {

        Log.d("StepFactoryImpl", "replied: " + step.getClass().getSimpleName() + " " + step);
        StepCoordinator.replyWith(step.getClass().hashCode(), impl);
    }

    protected <S extends UIStep> S createActivityBlocking(Class<?> cls) throws InterruptedException {
        return createActivityBlocking(cls, 0);
    }

    protected synchronized <S extends UIStep> S createActivityBlocking(Class<?> cls, int flags) throws InterruptedException {
        return createActivityBlocking(cls, flags, null);
    }

    protected synchronized <S extends UIStep> S createActivityBlocking(Class<?> cls, int flags, Bundle data) throws InterruptedException {

        createActivity(cls, flags, data == null ? new Bundle() : data);
        Log.d("StepFactoryImpl", "wait for: " + cls);
        S step = StepCoordinator.waitFor(cls.hashCode());

        stepsMap.put(cls, step);
        return step;
    }

    protected synchronized <S extends UIStep> S createFragmentBlocking(Class<?> ActivityClass, Class<?> fragmentClass, int flags, Bundle data) throws InterruptedException {

        data = data == null ? new Bundle() : data;
        data.putString(FRAGMENT, fragmentClass.getName());
        createActivity(ActivityClass, flags, data);
        Log.d("StepFactoryImpl", "wait for: " + fragmentClass);
        S step = StepCoordinator.waitFor(fragmentClass.hashCode());

        stepsMap.put(fragmentClass, step);
        return step;
    }

    protected void createActivity(Class<?> cls) {
        createActivity(cls, 0, null);
    }

    protected void createActivity(Class<?> cls, int flags) {
        createActivity(cls, flags, null);
    }

    private void createActivity(Class<?> cls, int flags, Bundle data) {
        Log.i("StepFactoryImpl", cls.getCanonicalName());
        if (topActivity != null) {
            Context context = topActivity;
            Intent intent = new Intent(context, cls).setFlags(flags);
            if (data != null) intent.putExtras(data);
            else intent.putExtra(NO_REPLY, true);
            context.startActivity(intent);
        } else {
            app.startActivity(new Intent(app, cls).addFlags(flags));
        }
    }

    @Deprecated
    protected synchronized <S> S retrieveActivity(Class<?> cls) throws InterruptedException {
        return retrieveActivity(cls, null);
    }

    @Deprecated
    protected synchronized <S> S retrieveActivity(Class<?> cls, Bundle data) throws InterruptedException {
        Intent intent = new Intent(ACTION_FETCH_REFERENCE);
        if (data != null) intent.putExtras(data);
        LocalBroadcastManager.getInstance(app).sendBroadcast(intent);
        return StepCoordinator.waitFor(cls.hashCode());
    }

    protected synchronized <S extends UIStep> S retrieveView(Class<?> cls, Bundle data) throws InterruptedException {

        UIStep step = stepsMap.get(cls);
        if (step != null) {
            step = step.onNewReference(data);
        }

        if (step == null) {
            return createActivityBlocking(cls, Intent.FLAG_ACTIVITY_SINGLE_TOP, data);
        } else {
            //noinspection unchecked
            return (S) step;
        }
    }

    @Override
    public void onActivityCreated(Activity activity, Bundle savedInstanceState) {

    }

    @Override
    public void onActivityStarted(Activity activity) {

    }

    @Override
    public void onActivityResumed(Activity activity) {
        topActivity = (A) activity;
    }

    @Override
    public void onActivityPaused(Activity activity) {

        if (activity.isFinishing())
            stepsMap.remove(activity.getClass());
    }

    @Override
    public void onActivityStopped(Activity activity) {

        if (activity.isFinishing())
            stepsMap.remove(activity.getClass());

    }

    @Override
    public void onActivitySaveInstanceState(Activity activity, Bundle outState) {

    }

    @Override
    public void onActivityDestroyed(Activity activity) {
        stepsMap.remove(activity.getClass());
    }

}

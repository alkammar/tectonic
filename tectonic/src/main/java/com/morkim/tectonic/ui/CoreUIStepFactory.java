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

@SuppressWarnings({"WeakerAccess", "unused", "SameParameterValue"})
public class CoreUIStepFactory<A extends Activity>
        implements StepFactory,
        Application.ActivityLifecycleCallbacks {

    public static final String NO_REPLY = "no.reply";
    public static final String ACTION_FETCH_REFERENCE = "action.fetch.reference";

    private A topActivity;

    private Application app;

    public CoreUIStepFactory(Application app) {
        this.app = app;
        app.registerActivityLifecycleCallbacks(this);
    }

    @Override
    public <S> S create(Class<? extends Step> aClass) {
        return create(aClass, "");
    }

    @Override
    public <S> S create(Class<? extends Step> aClass, String instanceId) {
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

    protected <S> S createActivityBlocking(Class<?> cls) throws InterruptedException {
        return createActivityBlocking(cls, 0);
    }

    protected synchronized <S> S createActivityBlocking(Class<?> cls, int flags) throws InterruptedException {
        return createActivityBlocking(cls, flags, null);
    }

    protected synchronized <S> S createActivityBlocking(Class<?> cls, int flags, Bundle data) throws InterruptedException {

        createActivity(cls, flags, data == null ? new Bundle() : data);
        Log.d("StepFactoryImpl", "wait for: " + cls);
        return StepCoordinator.waitFor(cls.hashCode());
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

    protected synchronized <S> S retrieveActivity(Class<?> cls) throws InterruptedException {
        return retrieveActivity(cls, null);
    }

    private synchronized <S> S retrieveActivity(Class<?> cls, Bundle data) throws InterruptedException {
        Intent intent = new Intent(ACTION_FETCH_REFERENCE);
        if (data != null) intent.putExtras(data);
        LocalBroadcastManager.getInstance(app).sendBroadcast(intent);
        return StepCoordinator.waitFor(cls.hashCode());
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

    }

    @Override
    public void onActivityStopped(Activity activity) {

    }

    @Override
    public void onActivitySaveInstanceState(Activity activity, Bundle outState) {

    }

    @Override
    public void onActivityDestroyed(Activity activity) {

    }

}

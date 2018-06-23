package com.morkim.usecase.di;

import android.content.Context;

import com.morkim.tectonic.flow.StepFactory;
import com.morkim.tectonic.simplified.Triggers;
import com.morkim.usecase.app.AppTrigger;
import com.morkim.usecase.model.Profile;

import javax.inject.Singleton;

import dagger.Component;


@Singleton
@Component(modules = AppModule.class)
public interface AppComponent {

    Context getContext();

    Profile getProfile();

    Triggers<AppTrigger.Event> triggers();

    StepFactory stepFactory();
}

package com.morkim.usecase.di;

import android.content.Context;

import com.morkim.tectonic.flow.StepFactory;
import com.morkim.tectonic.usecase.Triggers;
import com.morkim.usecase.app.AppTrigger;
import com.morkim.usecase.app.PoC;
import com.morkim.usecase.auth.AuthenticationFlow;
import com.morkim.usecase.model.Profile;

import javax.inject.Singleton;

import dagger.Component;


@Singleton
@Component(modules = AppModule.class)
public interface AppComponent {

    PoC poc();

    Context context();

    AuthenticationFlow authentication();

    Triggers<AppTrigger.Event> triggers();

    StepFactory stepFactory();
}

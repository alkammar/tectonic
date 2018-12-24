package com.morkim.usecase.di;

import android.content.Context;

import com.morkim.tectonic.flow.StepFactory;
import com.morkim.tectonic.usecase.Triggers;
import com.morkim.usecase.app.PoC;
import com.morkim.usecase.app.UseCaseExecutor;
import com.morkim.usecase.auth.AuthenticationFlow;

import javax.inject.Singleton;

import dagger.Component;


@Singleton
@Component(modules = AppModule.class)
public interface AppComponent {

    PoC poc();

    Context context();

    AuthenticationFlow authentication();

    Triggers<UseCaseExecutor.Event> triggers();

    StepFactory stepFactory();
}

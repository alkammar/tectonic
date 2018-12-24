package com.morkim.usecase.di.uc.main;

import com.morkim.tectonic.usecase.Triggers;
import com.morkim.usecase.app.UseCaseExecutor;
import com.morkim.usecase.auth.AuthenticationFlow;
import com.morkim.usecase.backend.BackendImpl;
import com.morkim.usecase.di.PerUseCase;
import com.morkim.usecase.uc.MainUseCase;

import dagger.Module;
import dagger.Provides;

@Module
public class MainUseCaseModule {

    private MainUseCase.UI ui;

    public MainUseCaseModule(MainUseCase.UI ui) {
        this.ui = ui;
    }

    @Provides
    @PerUseCase
    MainUseCase.Backend provideBackend(Triggers<UseCaseExecutor.Event> triggers) {
        return new BackendImpl(triggers);
    }

    @Provides
    @PerUseCase
    MainUseCase.Authenticator provideAuthenticator(AuthenticationFlow authenticationFlow) {
        return authenticationFlow;
    }

    @Provides
    @PerUseCase
    MainUseCase.UI provideUI() {
        return ui;
    }

}

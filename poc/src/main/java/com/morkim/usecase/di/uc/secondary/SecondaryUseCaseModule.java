package com.morkim.usecase.di.uc.secondary;

import com.morkim.tectonic.flow.StepFactory;
import com.morkim.tectonic.usecase.Triggers;
import com.morkim.usecase.app.UseCaseExecutor;
import com.morkim.usecase.auth.AuthenticationFlow;
import com.morkim.usecase.backend.BackendImpl;
import com.morkim.usecase.di.PerUseCase;
import com.morkim.usecase.flow.SecondaryFlowImpl;

import lib.morkim.uc.SecondaryUseCase;

import dagger.Module;
import dagger.Provides;

@Module
public class SecondaryUseCaseModule {

    @Provides
    @PerUseCase
    SecondaryUseCase.Backend provideBackend(Triggers<UseCaseExecutor.Event> triggers) {
        return new BackendImpl(triggers);
    }

    @Provides
    @PerUseCase
    SecondaryUseCase.Authenticator provideAuthenticator(AuthenticationFlow authenticationFlow) {
        return authenticationFlow;
    }

    @Provides
    @PerUseCase
    SecondaryUseCase.UI provideBaseUi(SecondaryUseCase.UI<UseCaseExecutor.Event> ui) {
        return ui;
    }

    @Provides
    @PerUseCase
    SecondaryUseCase.UI<UseCaseExecutor.Event> provideUi(StepFactory stepFactory) {
        return new SecondaryFlowImpl(stepFactory);
    }

}

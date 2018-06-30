package com.morkim.usecase.di.uc.secondary;

import com.morkim.usecase.auth.AuthenticationFlow;
import com.morkim.usecase.backend.BackendImpl;
import com.morkim.usecase.di.PerUseCase;
import com.morkim.usecase.uc.SecondaryUseCase;

import dagger.Module;
import dagger.Provides;

@Module
public class SecondaryUseCaseModule {

    private final SecondaryUseCase.UI ui;

    public SecondaryUseCaseModule(SecondaryUseCase.UI ui) {
        this.ui = ui;
    }

    @Provides
    @PerUseCase
    SecondaryUseCase.Backend provideBackend() {
        return new BackendImpl();
    }

    @Provides
    @PerUseCase
    SecondaryUseCase.Authenticator provideAuthenticator(AuthenticationFlow authenticationFlow) {
        return authenticationFlow;
    }

    @Provides
    @PerUseCase
    SecondaryUseCase.UI provideUser() {
        return ui;
    }

}

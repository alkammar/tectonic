package com.morkim.usecase.di.uc.regsitration;

import com.morkim.tectonic.usecase.Triggers;
import com.morkim.usecase.app.UseCaseExecutor;
import com.morkim.usecase.backend.BackendImpl;
import com.morkim.usecase.di.PerUseCase;
import com.morkim.usecase.uc.RegisterUser;

import dagger.Module;
import dagger.Provides;

@Module
public class RegisterUserModule {

    private final RegisterUser.UI ui;

    public RegisterUserModule(RegisterUser.UI ui) {
        this.ui = ui;
    }

    @Provides
    @PerUseCase
    RegisterUser.Backend provideBackend(Triggers<UseCaseExecutor.Event> triggers) {
        return new BackendImpl(triggers);
    }

    @Provides
    @PerUseCase
    RegisterUser.UI provideUi() {
        return ui;
    }

}

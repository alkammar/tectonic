package com.morkim.usecase.di.uc.logout;

import com.morkim.tectonic.flow.StepFactory;
import com.morkim.tectonic.usecase.Triggers;
import com.morkim.usecase.app.UseCaseExecutor;
import com.morkim.usecase.backend.BackendImpl;
import com.morkim.usecase.di.PerUseCase;
import com.morkim.usecase.flow.LogoutFlowImpl;
import com.morkim.usecase.uc.LogoutUser;

import dagger.Module;
import dagger.Provides;

@Module
public class LogoutUserModule {

    @Provides
    @PerUseCase
    LogoutUser.Backend provideBackend(Triggers<UseCaseExecutor.Event> triggers) {
        return new BackendImpl(triggers);
    }

    @Provides
    @PerUseCase
    LogoutUser.UI provideUI(StepFactory stepFactory) {
        return new LogoutFlowImpl(stepFactory);
    }

}

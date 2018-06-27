package com.morkim.usecase.di.uc.logout;

import com.morkim.tectonic.flow.StepFactory;
import com.morkim.usecase.backend.BackendImpl;
import com.morkim.usecase.di.PerUseCase;
import com.morkim.usecase.flow.LogoutFlowImpl;
import com.morkim.usecase.uc.logout.LogoutUser;

import dagger.Module;
import dagger.Provides;

@Module
public class LogoutUserModule {

    @Provides
    @PerUseCase
    LogoutUser.Backend provideBackend() {
        return new BackendImpl();
    }

    @Provides
    @PerUseCase
    LogoutUser.UI provideUI(StepFactory stepFactory) {
        return new LogoutFlowImpl(stepFactory);
    }

}

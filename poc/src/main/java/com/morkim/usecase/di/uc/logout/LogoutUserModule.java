package com.morkim.usecase.di.uc.logout;

import com.morkim.usecase.app.PoC;
import com.morkim.usecase.backend.BackendImpl;
import com.morkim.usecase.di.PerUseCase;
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
    LogoutUser.UI provideUI(PoC poc) {
        return poc;
    }

}

package com.morkim.usecase.di.uc.main;

import com.morkim.usecase.di.PerUseCase;
import com.morkim.usecase.uc.MainUseCase;

import dagger.Module;
import dagger.Provides;

@Module
public class MainUseCaseModule {

    private MainUseCase.User user;

    public MainUseCaseModule(MainUseCase.User user) {
        this.user = user;
    }

    @Provides
    @PerUseCase
    MainUseCase.User provideUser() {
        return user;
    }

}

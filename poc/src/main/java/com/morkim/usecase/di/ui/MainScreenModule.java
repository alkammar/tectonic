package com.morkim.usecase.di.ui;

import com.morkim.usecase.di.PerActivity;
import com.morkim.usecase.uc.MainUseCase;
import com.morkim.usecase.ui.MainActivity;

import dagger.Module;
import dagger.Provides;

@Module
public class MainScreenModule {

    private MainActivity activity;

    public MainScreenModule(MainActivity activity) {
        this.activity = activity;
    }

    @Provides
    @PerActivity
    MainUseCase.User provideUser() {
        return activity;
    }
}

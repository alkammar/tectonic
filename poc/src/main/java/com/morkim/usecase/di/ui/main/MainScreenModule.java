package com.morkim.usecase.di.ui.main;

import com.morkim.usecase.di.PerActivity;
import com.morkim.usecase.uc.main.MainUseCase;
import com.morkim.usecase.ui.main.MainActivity;

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
    MainUseCase.UI provideUI() {
        return activity;
    }
}

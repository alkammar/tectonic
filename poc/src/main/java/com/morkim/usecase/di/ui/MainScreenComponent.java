package com.morkim.usecase.di.ui;

import android.content.Context;

import com.morkim.tectonic.simplified.Triggers;
import com.morkim.usecase.di.AppComponent;
import com.morkim.usecase.di.PerActivity;
import com.morkim.usecase.di.PerApp;
import com.morkim.usecase.uc.MainUseCase;
import com.morkim.usecase.ui.MainActivity;

import dagger.Component;

@PerActivity
@Component(dependencies = {AppComponent.class}, modules = {MainScreenModule.class})
public interface MainScreenComponent {

    void inject(MainActivity MainActivity);

    MainUseCase.User user();
}

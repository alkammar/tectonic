package com.morkim.usecase.di.ui;

import com.morkim.usecase.di.AppComponent;
import com.morkim.usecase.di.PerActivity;
import com.morkim.usecase.uc.main.MainUseCase;
import com.morkim.usecase.ui.main.MainActivity;

import dagger.Component;

@PerActivity
@Component(dependencies = {AppComponent.class}, modules = {MainScreenModule.class})
public interface MainScreenComponent {

    void inject(MainActivity MainActivity);

    MainUseCase.UI user();
}

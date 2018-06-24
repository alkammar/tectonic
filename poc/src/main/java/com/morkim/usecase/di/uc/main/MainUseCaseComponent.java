package com.morkim.usecase.di.uc.main;

import com.morkim.usecase.di.AppComponent;
import com.morkim.usecase.di.PerUseCase;
import com.morkim.usecase.uc.main.MainUseCase;

import dagger.Component;

@PerUseCase
@Component(dependencies = {AppComponent.class}, modules = {MainUseCaseModule.class})
public interface MainUseCaseComponent {

    void inject(MainUseCase mainUseCase);
}

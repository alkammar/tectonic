package com.morkim.usecase.di.uc.secondary;

import com.morkim.usecase.di.AppComponent;
import com.morkim.usecase.di.PerUseCase;

import dagger.Component;

@PerUseCase
@Component(dependencies = {AppComponent.class}, modules = {SecondaryUseCaseModule.class})
public interface SecondaryUseCaseComponent extends lib.morkim.di.SecondaryUseCaseComponent {

}

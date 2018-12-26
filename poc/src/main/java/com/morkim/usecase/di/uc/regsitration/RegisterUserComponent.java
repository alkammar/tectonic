package com.morkim.usecase.di.uc.regsitration;

import com.morkim.usecase.di.AppComponent;
import com.morkim.usecase.di.PerUseCase;
import com.morkim.usecase.uc.RegisterUser;

import dagger.Component;

@PerUseCase
@Component(dependencies = {AppComponent.class}, modules = {RegisterUserModule.class})
public interface RegisterUserComponent {

    void inject(RegisterUser registerUser);
}

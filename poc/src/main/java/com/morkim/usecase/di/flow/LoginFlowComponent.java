package com.morkim.usecase.di.flow;

import com.morkim.usecase.di.AppComponent;
import com.morkim.usecase.di.PerUseCase;
import com.morkim.usecase.uc.LoginUser;

import dagger.Component;

@PerUseCase
@Component(dependencies = {AppComponent.class}, modules = {LoginFlowModule.class})
public interface LoginFlowComponent {

    void inject(LoginUser loginUser);
}

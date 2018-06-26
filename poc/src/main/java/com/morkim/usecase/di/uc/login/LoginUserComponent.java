package com.morkim.usecase.di.uc.login;

import com.morkim.tectonic.flow.StepFactory;
import com.morkim.usecase.contract.login.Login;
import com.morkim.usecase.di.AppComponent;
import com.morkim.usecase.di.PerUseCase;
import com.morkim.usecase.uc.login.LoginUser;

import dagger.Component;

@PerUseCase
@Component(dependencies = {AppComponent.class}, modules = {LoginUserModule.class})
public interface LoginUserComponent {

    void inject(LoginUser loginUser);

    StepFactory stepFactory();

    Login.Flow flow();
}

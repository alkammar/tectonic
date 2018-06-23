package com.morkim.usecase.di.ui.login;

import com.morkim.usecase.contract.login.Login;
import com.morkim.usecase.di.AppComponent;
import com.morkim.usecase.di.PerActivity;
import com.morkim.usecase.di.uc.login.LoginUserComponent;
import com.morkim.usecase.ui.LoginActivity;

import dagger.Component;

@PerActivity
@Component(dependencies = {LoginUserComponent.class}, modules = {LoginScreenModule.class})
public interface LoginScreenComponent {

    void inject(LoginActivity loginActivity);
}

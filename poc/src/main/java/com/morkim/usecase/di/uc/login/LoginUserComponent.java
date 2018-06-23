package com.morkim.usecase.di.uc.login;

import com.morkim.tectonic.flow.StepFactory;
import com.morkim.tectonic.simplified.PrimaryActor;
import com.morkim.usecase.app.AppTrigger;
import com.morkim.usecase.contract.login.Login;
import com.morkim.usecase.di.AppComponent;
import com.morkim.usecase.di.PerUseCase;
import com.morkim.usecase.uc.LoginUser;

import dagger.Component;

@PerUseCase
@Component(dependencies = {AppComponent.class}, modules = {LoginModule.class})
public interface LoginUserComponent {

    void inject(LoginUser loginUser);

    StepFactory stepFactory();

    Login.Flow flow();

    PrimaryActor<AppTrigger.Event,Void> primaryActor();
}

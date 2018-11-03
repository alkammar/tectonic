package com.morkim.usecase.di.flow;

import com.morkim.usecase.contract.Registration;
import com.morkim.usecase.di.AppComponent;
import com.morkim.usecase.di.PerUseCase;
import com.morkim.usecase.uc.RegisterUser;

import dagger.Component;

@PerUseCase
@Component(dependencies = {AppComponent.class}, modules = {RegistrationFlowModule.class})
public interface RegistrationFlowComponent {

    RegisterUser.UI ui();

    Registration.Flow flow();
}

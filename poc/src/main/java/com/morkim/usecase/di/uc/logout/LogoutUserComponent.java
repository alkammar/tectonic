package com.morkim.usecase.di.uc.logout;

import com.morkim.usecase.di.AppComponent;
import com.morkim.usecase.di.PerUseCase;
import com.morkim.usecase.uc.logout.LogoutUser;

import dagger.Component;

@PerUseCase
@Component(dependencies = {AppComponent.class}, modules = {LogoutUserModule.class})
public interface LogoutUserComponent {

    void inject(LogoutUser logoutUser);
}

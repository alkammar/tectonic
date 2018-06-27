package com.morkim.usecase.di.flow;

import com.morkim.tectonic.flow.StepFactory;
import com.morkim.usecase.di.PerUseCase;
import com.morkim.usecase.flow.LoginFlowImpl;
import com.morkim.usecase.uc.login.InvalidLogin;
import com.morkim.usecase.uc.login.LoginUser;

import dagger.Module;
import dagger.Provides;

@Module
public class LoginFlowModule {

    @Provides
    @PerUseCase
    LoginUser.UI provideUser(StepFactory stepFactory) {
        return new LoginFlowImpl(stepFactory);
    }

    @Provides
    @PerUseCase
    LoginUser.Authenticator provideAuthenticator() {
        return password -> {
            if (password.equals("asdf")) throw new InvalidLogin();
        };
    }

}

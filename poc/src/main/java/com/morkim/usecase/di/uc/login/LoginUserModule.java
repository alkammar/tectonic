package com.morkim.usecase.di.uc.login;

import com.morkim.tectonic.usecase.PrimaryActor;
import com.morkim.usecase.app.AppTrigger;
import com.morkim.usecase.auth.AuthenticationFlow;
import com.morkim.usecase.contract.Login;
import com.morkim.usecase.di.PerUseCase;
import com.morkim.usecase.uc.login.InvalidLogin;
import com.morkim.usecase.uc.login.LoginUser;

import dagger.Module;
import dagger.Provides;

@Module
public class LoginUserModule {

    @Provides
    @PerUseCase
    PrimaryActor<AppTrigger.Event, Void> provideUI(AuthenticationFlow authenticationFlow) {
        return authenticationFlow;
    }

    @Provides
    @PerUseCase
    LoginUser.UI provideUser(AuthenticationFlow authenticationFlow) {
        return authenticationFlow;
    }

    @Provides
    @PerUseCase
    LoginUser.Authenticator provideAuthenticator() {
        return password -> { if (password.equals("asdf")) throw new InvalidLogin(); };
    }

    @Provides
    @PerUseCase
    Login.Flow provideFlow(AuthenticationFlow authenticationFlow) {
        return authenticationFlow;
    }

}

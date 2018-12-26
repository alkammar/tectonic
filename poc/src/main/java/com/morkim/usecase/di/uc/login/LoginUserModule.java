package com.morkim.usecase.di.uc.login;

import com.morkim.tectonic.usecase.Triggers;
import com.morkim.usecase.app.UseCaseExecutor;
import com.morkim.usecase.auth.AuthenticationFlow;
import com.morkim.usecase.backend.BackendImpl;
import com.morkim.usecase.contract.Login;
import com.morkim.usecase.di.PerUseCase;
import com.morkim.usecase.uc.LoginUser;

import dagger.Module;
import dagger.Provides;

@Module
public class LoginUserModule {

    @Provides
    @PerUseCase
    LoginUser.UI provideUi(AuthenticationFlow authenticationFlow) {
        return authenticationFlow;
    }

    @Provides
    @PerUseCase
    LoginUser.Backend provideAuthenticator(Triggers<UseCaseExecutor.Event> triggers) {
        return new BackendImpl(triggers);
    }

    @Provides
    @PerUseCase
    Login.Flow provideFlow(AuthenticationFlow authenticationFlow) {
        return authenticationFlow;
    }

}

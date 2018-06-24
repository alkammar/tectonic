package com.morkim.usecase.di.uc.login;

import com.morkim.tectonic.flow.StepFactory;
import com.morkim.tectonic.simplified.PrimaryActor;
import com.morkim.tectonic.simplified.Triggers;
import com.morkim.usecase.app.AppTrigger;
import com.morkim.usecase.contract.login.Login;
import com.morkim.usecase.di.PerUseCase;
import com.morkim.usecase.flow.login.LoginFlowImpl;
import com.morkim.usecase.uc.login.InvalidLogin;
import com.morkim.usecase.uc.login.LoginUser;

import dagger.Module;
import dagger.Provides;

@Module
public class LoginModule {


    @Provides
    @PerUseCase
    LoginFlowImpl provideLoginFlow(Triggers<AppTrigger.Event> triggers, StepFactory stepFactory) {
        return new LoginFlowImpl(triggers, stepFactory);
    }

    @Provides
    @PerUseCase
    PrimaryActor<AppTrigger.Event, Void> provideUI(LoginFlowImpl loginFlow) {
        return loginFlow;
    }

    @Provides
    @PerUseCase
    LoginUser.User provideUser(LoginFlowImpl loginFlow) {
        return loginFlow;
    }

    @Provides
    @PerUseCase
    LoginUser.Authenticator provideAuthenticator() {
        return password -> { if (password.equals("asdf")) throw new InvalidLogin(); };
    }

    @Provides
    @PerUseCase
    Login.Flow provideFlow(LoginFlowImpl loginFlow) {
        return loginFlow;
    }

}

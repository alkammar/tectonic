package com.morkim.usecase.di.flow;

import com.morkim.tectonic.flow.StepFactory;
import com.morkim.tectonic.simplified.Triggers;
import com.morkim.usecase.app.AppTrigger;
import com.morkim.usecase.di.PerUseCase;
import com.morkim.usecase.flow.login.LoginFlowImpl;
import com.morkim.usecase.uc.InvalidLogin;
import com.morkim.usecase.uc.LoginUser;

import dagger.Module;
import dagger.Provides;

@Module
public class LoginFlowModule {

    @Provides
    @PerUseCase
    LoginUser.User provideUser(Triggers<AppTrigger.Event> triggers, StepFactory stepFactory) {
        return new LoginFlowImpl(triggers, stepFactory);
    }

    @Provides
    @PerUseCase
    LoginUser.Authenticator provideAuthenticator() {
        return password -> {
            if (password.equals("asdf")) throw new InvalidLogin();
        };
    }

}

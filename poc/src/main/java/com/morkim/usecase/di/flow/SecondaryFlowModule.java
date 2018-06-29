package com.morkim.usecase.di.flow;

import com.morkim.tectonic.flow.StepFactory;
import com.morkim.usecase.contract.Secondary;
import com.morkim.usecase.di.PerUseCase;
import com.morkim.usecase.flow.SecondaryFlowImpl;
import com.morkim.usecase.uc.login.InvalidLogin;
import com.morkim.usecase.uc.login.LoginUser;
import com.morkim.usecase.uc.secondary.SecondaryUseCase;

import dagger.Module;
import dagger.Provides;

@Module
public class SecondaryFlowModule {

    @Provides
    @PerUseCase
    SecondaryFlowImpl provideFlowImpl(StepFactory stepFactory) {
        return new SecondaryFlowImpl(stepFactory);
    }

    @Provides
    @PerUseCase
    Secondary.Flow provideFlow(SecondaryFlowImpl secondaryFlow) {
        return secondaryFlow;
    }

    @Provides
    @PerUseCase
    SecondaryUseCase.UI provideUI(SecondaryFlowImpl secondaryFlow) {
        return secondaryFlow;
    }

}

package com.morkim.usecase.di.flow;

import com.morkim.tectonic.flow.StepFactory;
import com.morkim.usecase.contract.Registration;
import com.morkim.usecase.contract.Secondary;
import com.morkim.usecase.di.PerUseCase;
import com.morkim.usecase.flow.RegistrationFlowImpl;
import com.morkim.usecase.flow.SecondaryFlowImpl;
import com.morkim.usecase.uc.RegisterUser;
import com.morkim.usecase.uc.SecondaryUseCase;

import dagger.Module;
import dagger.Provides;

@Module
public class RegistrationFlowModule {

    @Provides
    @PerUseCase
    RegistrationFlowImpl provideFlowImpl(StepFactory stepFactory) {
        return new RegistrationFlowImpl(stepFactory);
    }

    @Provides
    @PerUseCase
    Registration.Flow provideFlow(RegistrationFlowImpl registrationFlow) {
        return registrationFlow;
    }

    @Provides
    @PerUseCase
    RegisterUser.UI provideUI(RegistrationFlowImpl registrationFlow) {
        return registrationFlow;
    }

}

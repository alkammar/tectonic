package com.morkim.usecase.di.ui.registration;

import com.morkim.usecase.contract.Registration;
import com.morkim.usecase.di.PerActivity;

import dagger.Module;
import dagger.Provides;

@Module
public class Registration1ActivityModule {

    private Registration.Flow flow;

    public Registration1ActivityModule(Registration.Flow flow) {
        this.flow = flow;
    }

    @Provides
    @PerActivity
    Registration.Flow provideFlow() {
        return flow;
    }
}

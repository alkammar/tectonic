package com.morkim.usecase.di.ui.secondary;

import com.morkim.usecase.contract.Secondary;
import com.morkim.usecase.di.PerActivity;

import dagger.Module;
import dagger.Provides;

@Module
public class SecondaryScreen1Module {

    private Secondary.Flow flow;

    public SecondaryScreen1Module(Secondary.Flow flow) {
        this.flow = flow;
    }

    @Provides
    @PerActivity
    Secondary.Flow provideFlow() {
        return flow;
    }
}

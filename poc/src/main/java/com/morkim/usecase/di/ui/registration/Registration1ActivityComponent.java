package com.morkim.usecase.di.ui.registration;

import com.morkim.usecase.di.AppComponent;
import com.morkim.usecase.di.PerActivity;
import com.morkim.usecase.ui.registration.Registration1Activity;

import dagger.Component;

@PerActivity
@Component(dependencies = {AppComponent.class}, modules = {Registration1ActivityModule.class})
public interface Registration1ActivityComponent {

    void inject(Registration1Activity registration1Activity);
}

package com.morkim.usecase.di.ui.registration;

import com.morkim.usecase.di.AppComponent;
import com.morkim.usecase.di.PerActivity;
import com.morkim.usecase.ui.registration.Registration2Activity;

import dagger.Component;

@PerActivity
@Component(dependencies = {AppComponent.class}, modules = {Registration2ActivityModule.class})
public interface Registration2ActivityComponent {

    void inject(Registration2Activity registration2Activity);
}

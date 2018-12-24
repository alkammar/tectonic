package com.morkim.usecase.di.ui.secondary;

import com.morkim.usecase.di.AppComponent;
import com.morkim.usecase.di.PerActivity;
import com.morkim.usecase.ui.secondary.SecondaryActivity3;

import dagger.Component;

@PerActivity
@Component(dependencies = {AppComponent.class}, modules = {SecondaryScreen3Module.class})
public interface SecondaryScreen3Component {

    void inject(SecondaryActivity3 secondaryActivity3);
}

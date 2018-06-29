package com.morkim.usecase.di.ui.secondary;

import com.morkim.usecase.di.AppComponent;
import com.morkim.usecase.di.PerActivity;
import com.morkim.usecase.ui.secondary.SecondaryActivity1;

import dagger.Component;

@PerActivity
@Component(dependencies = {AppComponent.class}, modules = {SecondaryScreen1Module.class})
public interface SecondaryScreen1Component {

    void inject(SecondaryActivity1 secondaryActivity1);
}

package com.morkim.usecase.di.ui.secondary;

import com.morkim.usecase.di.AppComponent;
import com.morkim.usecase.di.PerActivity;
import com.morkim.usecase.ui.secondary.SecondaryActivity1;
import com.morkim.usecase.ui.secondary.SecondaryActivity2;

import dagger.Component;

@PerActivity
@Component(dependencies = {AppComponent.class}, modules = {SecondaryScreen2Module.class})
public interface SecondaryScreen2Component {

    void inject(SecondaryActivity2 secondaryActivity2);
}

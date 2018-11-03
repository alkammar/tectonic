package com.morkim.usecase.di.flow;

import com.morkim.usecase.contract.Secondary;
import com.morkim.usecase.di.AppComponent;
import com.morkim.usecase.di.PerUseCase;

import dagger.Component;
import lib.morkim.uc.SecondaryUseCase;

@PerUseCase
@Component(dependencies = {AppComponent.class}, modules = {SecondaryFlowModule.class})
public interface SecondaryFlowComponent {

    SecondaryUseCase.UI ui();

    Secondary.Flow flow();
}

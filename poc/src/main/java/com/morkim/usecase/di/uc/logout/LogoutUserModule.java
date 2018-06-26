package com.morkim.usecase.di.uc.logout;

import android.content.Context;
import android.content.Intent;

import com.morkim.usecase.backend.BackendImpl;
import com.morkim.usecase.di.PerUseCase;
import com.morkim.usecase.uc.LogoutUser;
import com.morkim.usecase.uc.login.LoginUser;
import com.morkim.usecase.ui.login.LoginActivity;
import com.morkim.usecase.ui.main.MainActivity;

import dagger.Module;
import dagger.Provides;

@Module
public class LogoutUserModule {

    @Provides
    @PerUseCase
    LogoutUser.Backend provideBackend() {
        return new BackendImpl();
    }

    @Provides
    @PerUseCase
    LogoutUser.UI provideUI(Context context) {
        return () -> {
            Intent intent = new Intent(context, MainActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            context.startActivity(intent);
        };
    }

}

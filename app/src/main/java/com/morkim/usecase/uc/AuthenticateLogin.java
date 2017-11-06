package com.morkim.usecase.uc;

import android.util.Log;

import com.morkim.tectonic.Result;
import com.morkim.tectonic.UseCase;


public class AuthenticateLogin extends UseCase<AuthenticateLoginRequest, Result> {

    public static final int PASSWORD = 1;

    @Override
    protected void onExecute(AuthenticateLoginRequest request) {

        Log.i(this.getClass().getSimpleName(), "onExecute : " + Thread.currentThread().getName());

        if (request == null || request.password.isEmpty())
            requestInput(PASSWORD);
        else
            finish();
    }
}

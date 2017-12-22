package com.morkim.usecase.uc;

import com.morkim.tectonic.Result;
import com.morkim.tectonic.UseCase;


public class ExitApp extends UseCase<RegisterUserRequest, Result> {

    @Override
    protected void onExecute(RegisterUserRequest request) {

        notifySubscribers(new Result() {});
        finish();
    }

}

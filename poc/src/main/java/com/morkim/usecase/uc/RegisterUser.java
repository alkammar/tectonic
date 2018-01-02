package com.morkim.usecase.uc;

import com.morkim.tectonic.Result;
import com.morkim.tectonic.UseCase;
import com.morkim.usecase.di.AppInjector;
import com.morkim.usecase.model.Profile;

import javax.inject.Inject;


public class RegisterUser extends UseCase<RegisterUserRequest, Result> {

    public static final int EMAIL = 1;
    public static final int PASSWORD = 2;
    public static final int MOBILE = 3;

    public static final int USER = 1;

    @Inject
    Profile profile = AppInjector.getAppComponent().getProfile();

    @Override
    protected void onExecute(RegisterUserRequest request) {

        if (request == null) {
            requestAction(USER, EMAIL, PASSWORD, MOBILE);
        } else {

            // Validate the registration inputs (from business model perspective)
            if (startInputValidation()
                    .check(request.email.isEmpty(), EMAIL)
                    .check(request.password.isEmpty(), PASSWORD)
                    .check(request.mobile.isEmpty(), MOBILE)
                    .validate()) {

                profile.setRegistered(true);

                // Registration is done, finish the use case so other blocking use cases can
                // continue their execution
                finish();
            }
        }
    }
}

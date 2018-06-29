package com.morkim.usecase.backend;

import android.os.SystemClock;

import com.morkim.usecase.model.SecondaryModel;
import com.morkim.usecase.uc.logout.LogoutUser;
import com.morkim.usecase.uc.main.ExpiredCredentials;
import com.morkim.usecase.uc.main.MainUseCase;
import com.morkim.usecase.uc.secondary.GeneralBackendError;
import com.morkim.usecase.uc.secondary.SecondaryUseCase;
import com.morkim.usecase.uc.secondary.SpecificBackendError;

public class BackendImpl implements MainUseCase.Backend, LogoutUser.Backend, SecondaryUseCase.Backend {

    private static boolean isExpired = true;

    @Override
    public String retrieveSomeData() throws ExpiredCredentials {

        if (isExpired) {
            isExpired = false;
            throw new ExpiredCredentials();
        }

        return toString();
    }

    @Override
    public SecondaryModel requestSomething(String data1, String data2, double data3) throws ExpiredCredentials, GeneralBackendError, SpecificBackendError {

        if (isExpired) {
            isExpired = false;
            throw new ExpiredCredentials();
        }

        SystemClock.sleep(1000);

        if (data1.isEmpty()) throw new GeneralBackendError();
        if (data3 < 10) throw new SpecificBackendError();

        return new SecondaryModel();
    }

    @Override
    public boolean logout() {
        isExpired = true;
        return true;
    }
}

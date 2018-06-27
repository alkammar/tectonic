package com.morkim.usecase.backend;

import com.morkim.usecase.uc.logout.LogoutUser;
import com.morkim.usecase.uc.main.ExpiredCredentials;
import com.morkim.usecase.uc.main.MainUseCase;

public class BackendImpl implements MainUseCase.Backend, LogoutUser.Backend {

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
    public boolean logout() {
        isExpired = true;
        return true;
    }
}

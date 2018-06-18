package com.morkim.usecase.uc;


import com.morkim.tectonic.simplified.UseCase;

public class ExitApp extends UseCase {

    @Override
    protected void onExecute() {
        finish();
    }
}

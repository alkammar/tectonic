package com.morkim.usecase.ui;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.EditText;

import com.morkim.tectonic.simplified.UseCase;
import com.morkim.usecase.R;
import com.morkim.usecase.uc.InvalidLogin;
import com.morkim.usecase.uc.Login;


public class LoginActivity extends AppCompatActivity implements Login.User {

    private static final int PASSWORD = 1;
    private EditText password;
    private View submit;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.screen_login);

        //noinspection ConstantConditions
        getSupportActionBar().setTitle("Login");

        password = (EditText) findViewById(R.id.ti_password);
        findViewById(R.id.btn_submit).setOnClickListener(v -> UseCase.replyWith(PASSWORD, password.getText().toString()));
    }

    @Override
    public void onBackPressed() {
        finishAffinity();
    }

    @Override
    public String askToEnterUserName() throws InterruptedException {
        return UseCase.immediate("");
    }

    @Override
    public String askToEnterPassword() throws InterruptedException {
        return UseCase.waitFor(PASSWORD);
    }

    @Override
    public void handle(Exception e) {
        if (e instanceof InvalidLogin)
            password.setError("Wrong password!");
    }
}

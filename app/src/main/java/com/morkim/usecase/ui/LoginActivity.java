package com.morkim.usecase.ui;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.EditText;

import com.morkim.tectonic.Result;
import com.morkim.tectonic.SimpleDisposableUseCaseListener;
import com.morkim.tectonic.SimpleUseCaseListener;
import com.morkim.tectonic.UseCase;
import com.morkim.usecase.R;
import com.morkim.usecase.uc.AuthenticateLogin;
import com.morkim.usecase.uc.AuthenticateLoginRequest;
import com.morkim.usecase.uc.ExitApp;


public class LoginActivity extends AppCompatActivity {

    private EditText password;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.screen_login);

        //noinspection ConstantConditions
        getSupportActionBar().setTitle("Login");

        password = (EditText) findViewById(R.id.ti_password);

        findViewById(R.id.btn_submit).setOnClickListener(v ->
                UseCase.fetch(AuthenticateLogin.class)
                        .subscribe(new SimpleUseCaseListener<Result>() {

                            @Override
                            public void onComplete() {
                                LoginActivity.this.finish();
                            }
                        })
                        .execute(new AuthenticateLoginRequest.Builder()
                                .password(password.getText().toString())
                                .build()));
    }

    @Override
    public void onBackPressed() {
        UseCase.fetch(ExitApp.class)
                .subscribe(new SimpleDisposableUseCaseListener<Result>() {
                    @Override
                    public void onComplete() {
                        LoginActivity.this.finish();
                    }
                })
                .execute();
    }
}

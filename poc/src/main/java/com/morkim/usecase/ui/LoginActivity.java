package com.morkim.usecase.ui;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.EditText;

import com.morkim.tectonic.Result;
import com.morkim.tectonic.SimpleDisposableUseCaseListener;
import com.morkim.tectonic.SimpleUseCaseListener;
import com.morkim.tectonic.UseCase;
import com.morkim.usecase.R;
import com.morkim.usecase.uc.Login;
import com.morkim.usecase.uc.AuthenticateLoginRequest;
import com.morkim.usecase.uc.ExitApp;
import com.morkim.usecase.uc.InvalidLogin;


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
                UseCase.fetch(Login.class)
                        .subscribe(authenticateLoginListener)
                        .execute(new AuthenticateLoginRequest.Builder()
                                .password(password.getText().toString())
                                .build()));
    }

    private SimpleUseCaseListener<Result> authenticateLoginListener = new SimpleUseCaseListener<Result>() {

        @Override
        public void onComplete() {
            // We are now logged in, so finish the Login screen
            LoginActivity.this.finish();
        }

        @Override
        public boolean onError(Throwable throwable) {

            if (throwable instanceof InvalidLogin)
                password.setError("Wrong password!");

            return true;
        }
    };

    @Override
    public void onBackPressed() {
        // Exiting from login means we need to exit the app, although we are on top of the main
        // screen, the main should listen to this and finish as well
        UseCase.fetch(ExitApp.class)
                .subscribe(new SimpleDisposableUseCaseListener<Result>() {
                    @Override
                    public void onComplete() {
                        LoginActivity.this.finish();
                    }
                })
                .execute();
    }

    @Override
    protected void onStop() {
        super.onStop();

        UseCase.unsubscribe(Login.class, authenticateLoginListener);
    }
}

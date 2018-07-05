package com.morkim.usecase.ui.login;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.EditText;

import com.morkim.tectonic.flow.StepFactory;
import com.morkim.usecase.R;
import com.morkim.usecase.contract.Login;
import com.morkim.usecase.di.AppInjector;
import com.morkim.usecase.di.ui.login.DaggerLoginScreenComponent;
import com.morkim.usecase.uc.InvalidLogin;

import javax.inject.Inject;


public class LoginActivity extends AppCompatActivity implements Login.Screen {

    private EditText password;

    @Inject
    StepFactory stepFactory;

    @Inject
    Login.Flow flow;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        AppInjector.setLoginScreenComponent(
                DaggerLoginScreenComponent.builder()
                .loginUserComponent(AppInjector.getLoginUserComponent())
                .build()
        );

        AppInjector.getLoginScreenComponent().inject(this);

        stepFactory.onCreated(this);

        setContentView(R.layout.screen_login);

        //noinspection ConstantConditions
        getSupportActionBar().setTitle("Login");

        password = findViewById(R.id.ti_password);
        findViewById(R.id.btn_submit)
                .setOnClickListener(v -> flow.submit(password.getText().toString()));

        findViewById(R.id.btn_register)
                .setOnClickListener(v -> flow.notRegistered());
    }

    @Override
    public void onBackPressed() {
        finishAffinity();
    }

    @Override
    public void handle(Exception e) {
        if (e instanceof InvalidLogin)
            runOnUiThread(() -> password.setError("Wrong password!"));
    }

    @Override
    public void terminate() {
        finish();
    }
}

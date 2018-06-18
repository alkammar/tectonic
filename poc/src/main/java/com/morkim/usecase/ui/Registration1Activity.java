package com.morkim.usecase.ui;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.EditText;

import com.morkim.usecase.R;
import com.morkim.usecase.contract.RegistrationFlow;
import com.morkim.usecase.uc.EmptyEmail;
import com.morkim.usecase.uc.EmptyPassword;
import com.morkim.usecase.uc.InvalidEmail;

import javax.inject.Inject;


public class Registration1Activity extends AppCompatActivity implements RegistrationFlow.Step1 {

    private EditText email;
    private EditText password;

    @Inject
    RegistrationFlow.Flow flow;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.screen_registration_1);

        //noinspection ConstantConditions
        getSupportActionBar().setTitle("Registration - Credentials");

        email = (EditText) findViewById(R.id.ti_email);
        password = (EditText) findViewById(R.id.ti_password);

        findViewById(R.id.btn_submit)
                .setOnClickListener(v -> flow.submit(email.getText().toString(), password.getText().toString()));
    }

    @Override
    public void handle(Exception e) {
        if (e instanceof EmptyEmail)
            email.setError("Please enter your email");
        else if (e instanceof InvalidEmail)
            email.setError("Please enter a valid email");
        else if (e instanceof EmptyPassword)
            password.setError("Please enter your password");
    }

    @Override
    public void onBackPressed() {
        flow.goBack(this);
    }

    @Override
    public void terminate() {
        finish();
    }
}

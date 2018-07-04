package com.morkim.usecase.ui.registration;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.Editable;
import android.text.TextWatcher;
import android.widget.EditText;
import android.widget.TextView;

import com.morkim.tectonic.flow.StepFactory;
import com.morkim.usecase.R;
import com.morkim.usecase.contract.Registration;
import com.morkim.usecase.di.AppInjector;
import com.morkim.usecase.di.ui.registration.DaggerRegistration1ActivityComponent;
import com.morkim.usecase.di.ui.registration.Registration1ActivityModule;
import com.morkim.usecase.uc.EmptyEmail;
import com.morkim.usecase.uc.EmptyPassword;
import com.morkim.usecase.uc.InvalidEmail;
import com.morkim.usecase.uc.NonMatchingPasswords;
import com.morkim.usecase.uc.RegisterUser;

import javax.inject.Inject;


public class Registration1Activity extends AppCompatActivity implements Registration.Step1 {

    @Inject
    StepFactory stepFactory;

    @Inject
    Registration.Flow flow;

    private EditText email;
    private EditText password;
    private TextView strength;
    private EditText passwordConfirm;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        AppInjector.setRegistration1ActivityComponent(
                DaggerRegistration1ActivityComponent.builder()
                        .appComponent(AppInjector.getAppComponent())
                        .registration1ActivityModule(new Registration1ActivityModule(AppInjector.getRegistrationFlowComponent().flow()))
                        .build());

        AppInjector.getRegistration1ActivityComponent().inject(this);

        stepFactory.onCreated(this);

        setContentView(R.layout.screen_registration_1);

        //noinspection ConstantConditions
        getSupportActionBar().setTitle("Registration - Credentials");

        email = findViewById(R.id.ti_email);
        password = findViewById(R.id.ti_password);
        strength = findViewById(R.id.tv_strength);
        password.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                flow.submitPassword(s.toString());
            }
        });
        passwordConfirm = findViewById(R.id.ti_password_confirm);

        findViewById(R.id.btn_submit)
                .setOnClickListener(v -> flow.next(
                        email.getText().toString(),
                        password.getText().toString(),
                        passwordConfirm.getText().toString()));
    }

    @Override
    public void updatePasswordStrength(int strength) {
        StringBuilder text = new StringBuilder();
        for (int i = 0; i < strength; i++) text.append("-");
        runOnUiThread(() -> this.strength.setText(text.toString()));
    }

    @Override
    public void showError(Exception e) {
        runOnUiThread(() -> {
            if (e instanceof EmptyEmail)                email.setError("Please enter your email");
            else if (e instanceof InvalidEmail)         email.setError("Please enter a valid email");
            else if (e instanceof EmptyPassword)        password.setError("Please enter your password");
            else if (e instanceof NonMatchingPasswords) passwordConfirm.setError("Passwords not matching");
        });
    }

    @Override
    public void showError(int e) {
        runOnUiThread(() -> {
            switch (e) {
                case RegisterUser.UI.ERROR_EMPTY_EMAIL:
                    email.setError("Please enter your email");
                    break;
                case RegisterUser.UI.ERROR_INVALID_EMAIL:
                    email.setError("Please enter a valid email");
                    break;
                case RegisterUser.UI.ERROR_EMPTY_PASSWORD:
                    password.setError("Please enter your password");
                    break;
                case RegisterUser.UI.ERROR_NON_MATCHING_PASSWORDS:
                    passwordConfirm.setError("Passwords not matching");
                    break;
            }
        });
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

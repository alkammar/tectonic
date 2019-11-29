package com.morkim.usecase.ui.registration;

import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import android.widget.EditText;

import com.morkim.tectonic.flow.StepListener;
import com.morkim.usecase.R;
import com.morkim.usecase.contract.Registration;
import com.morkim.usecase.di.AppInjector;
import com.morkim.usecase.di.ui.registration.DaggerRegistration2ActivityComponent;
import com.morkim.usecase.di.ui.registration.Registration2ActivityModule;
import com.morkim.usecase.uc.RegisterUser;

import javax.inject.Inject;


public class Registration2Activity extends AppCompatActivity implements Registration.Step2 {

    @Inject
    StepListener stepListener;

    @Inject
    Registration.Flow flow;

    private EditText mobile;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        AppInjector.setRegistration2ActivityComponent(
                DaggerRegistration2ActivityComponent.builder()
                        .appComponent(AppInjector.getAppComponent())
                        .registration2ActivityModule(new Registration2ActivityModule(AppInjector.getRegistrationFlowComponent().flow()))
                        .build());

        AppInjector.getRegistration2ActivityComponent().inject(this);

        stepListener.onCreated(this);

        setContentView(R.layout.screen_registration_2);

        //noinspection ConstantConditions
        getSupportActionBar().setTitle("Registration - Mobile");

        mobile = findViewById(R.id.ti_mobile);

        findViewById(R.id.btn_submit).setOnClickListener(v ->
                flow.submit(mobile.getText().toString()));
    }

    @Override
    public void showError(int e) {
        runOnUiThread(() -> {
            switch (e) {
                case RegisterUser.UI.ERROR_EMPTY_MOBILE:
                    mobile.setError("Please enter your mobile number");
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

package com.morkim.usecase.ui;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.EditText;

import com.morkim.usecase.R;
import com.morkim.usecase.contract.RegistrationFlow;

import javax.inject.Inject;


public class Registration2Activity extends AppCompatActivity implements RegistrationFlow.Step2 {

    private EditText mobile;

    @Inject
    RegistrationFlow.Flow flow;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.screen_registration_2);

        //noinspection ConstantConditions
        getSupportActionBar().setTitle("Registration - Mobile");

        mobile = (EditText) findViewById(R.id.ti_mobile);

        findViewById(R.id.btn_submit).setOnClickListener(v ->
                flow.submit(mobile.getText().toString()));
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

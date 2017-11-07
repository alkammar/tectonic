package com.morkim.usecase.ui;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.EditText;

import com.morkim.tectonic.Result;
import com.morkim.tectonic.SimpleUseCaseListener;
import com.morkim.tectonic.UseCase;
import com.morkim.usecase.R;
import com.morkim.usecase.uc.RegisterUser;
import com.morkim.usecase.uc.RegisterUserRequest;


public class Registration2Activity extends AppCompatActivity {

    private EditText mobile;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.screen_registration_2);

        //noinspection ConstantConditions
        getSupportActionBar().setTitle("Registration - Mobile");

        mobile = (EditText) findViewById(R.id.ti_mobile);

        findViewById(R.id.btn_submit).setOnClickListener(v ->
                UseCase.fetch(RegisterUser.class)
                        .subscribe(registerUserListener)
                        .execute(new RegisterUserRequest.Builder()
                                .email(getIntent().getStringExtra("email"))
                                .password(getIntent().getStringExtra("password"))
                                .mobile(mobile.getText().toString())
                                .build()));
    }

    private final SimpleUseCaseListener<Result> registerUserListener = new SimpleUseCaseListener<Result>() {

        @Override
        public void onComplete() {
            Registration2Activity.this.finish();
        }
    };

    @Override
    protected void onStop() {
        super.onStop();

        UseCase.unsubscribe(RegisterUser.class, registerUserListener);
    }
}

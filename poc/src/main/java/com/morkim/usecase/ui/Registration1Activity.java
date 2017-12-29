package com.morkim.usecase.ui;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.EditText;

import com.morkim.tectonic.Result;
import com.morkim.tectonic.SimpleUseCaseListener;
import com.morkim.tectonic.UseCase;
import com.morkim.usecase.R;
import com.morkim.usecase.uc.RegisterUser;
import com.morkim.usecase.uc.RegisterUserRequest;

import java.util.List;


public class Registration1Activity extends AppCompatActivity {

    private EditText email;
    private EditText password;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.screen_registration_1);

        //noinspection ConstantConditions
        getSupportActionBar().setTitle("Registration - Credentials");

        email = (EditText) findViewById(R.id.ti_email);
        password = (EditText) findViewById(R.id.ti_password);

        findViewById(R.id.btn_submit).setOnClickListener(v ->
                UseCase.fetch(RegisterUser.class)
                        .subscribe(new SimpleUseCaseListener<Result>() {

                            @Override
                            public boolean onActionRequired(List<Integer> codes) {

                                if (ourInputsComplete(codes)) {
                                    if (codes.contains(RegisterUser.MOBILE)) {

                                        // Navigate to the second screen in the registration screens,
                                        // passing the data that we entered here
                                        Intent intent = new Intent(getBaseContext(), Registration2Activity.class);
                                        intent.putExtra("email", email.getText().toString());
                                        intent.putExtra("password", password.getText().toString());
                                        startActivity(intent);
                                    }
                                } else {
                                    // Maybe here we can show some error for the error texts
                                }
                                return super.onActionRequired(codes);
                            }

                            private boolean ourInputsComplete(List<Integer> codes) {
                                return !codes.contains(RegisterUser.EMAIL) &&
                                        !codes.contains(RegisterUser.PASSWORD);
                            }

                            @Override
                            public void onComplete() {
                                Registration1Activity.this.finish();
                            }
                        })
                        .execute(new RegisterUserRequest.Builder()
                                .email(email.getText().toString())
                                .password(password.getText().toString())
                                .build()));
    }

    @Override
    public void onBackPressed() {

    }
}

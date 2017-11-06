package com.morkim.usecase.ui;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

import com.morkim.tectonic.Result;
import com.morkim.tectonic.SimpleUseCaseListener;
import com.morkim.tectonic.UseCase;
import com.morkim.usecase.R;
import com.morkim.usecase.uc.AuthenticateLogin;
import com.morkim.usecase.uc.MainUseCase;


public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.screen_main);
        UseCase.subscribe(AuthenticateLogin.class, authenticateLoginListener);

        UseCase.fetch(MainUseCase.class)
                .subscribe(new SimpleUseCaseListener<Result>() {
                    @Override
                    public void onUpdate(Result result) {

                    }
                })
                .execute();
    }

    private SimpleUseCaseListener<Result> authenticateLoginListener = new SimpleUseCaseListener<Result>() {
        @Override
        public void onInputRequired(int code) {
            if (code == AuthenticateLogin.PASSWORD)
                startActivity(new Intent(getBaseContext(), LoginActivity.class));
        }
    };

    @Override
    protected void onDestroy() {
        super.onDestroy();

        UseCase.unsubscribe(AuthenticateLogin.class, authenticateLoginListener);
    }
}

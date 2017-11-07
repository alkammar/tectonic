package com.morkim.usecase.ui;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.morkim.tectonic.Result;
import com.morkim.tectonic.SimpleDisposableUseCaseListener;
import com.morkim.tectonic.SimpleUseCaseListener;
import com.morkim.tectonic.UseCase;
import com.morkim.usecase.R;
import com.morkim.usecase.uc.AuthenticateLogin;
import com.morkim.usecase.uc.ExitApp;
import com.morkim.usecase.uc.LogoutUser;
import com.morkim.usecase.uc.MainUseCase;
import com.morkim.usecase.uc.MainUseCaseResult;
import com.morkim.usecase.uc.RegisterUser;

import java.util.List;


public class MainActivity extends AppCompatActivity {

    private TextView label;
    private ProgressBar progress;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.screen_main);

        //noinspection ConstantConditions
        getSupportActionBar().setTitle("Home");

        label = (TextView) findViewById(R.id.label);
        progress = (ProgressBar) findViewById(R.id.prg_progress);

        UseCase.subscribe(RegisterUser.class, registerUserListener);
        UseCase.subscribe(AuthenticateLogin.class, authenticateLoginListener);

        UseCase.subscribe(ExitApp.class, new SimpleDisposableUseCaseListener<Result>() {
            @Override
            public void onComplete() {
                MainActivity.this.finish();
            }
        });

        findViewById(R.id.btn_logout).setOnClickListener(v ->
                UseCase.fetch(LogoutUser.class)
                        .subscribe(new SimpleUseCaseListener<Result>() {
                            @Override
                            public void onComplete() {
                                label.setText("");
                                UseCase.clearCache(MainUseCase.class);
                                startActivity(new Intent(getBaseContext(), LoginActivity.class));
                            }
                        })
                        .execute());
    }

    @Override
    protected void onStart() {
        super.onStart();

        UseCase.fetch(MainUseCase.class)
                .subscribe(new SimpleUseCaseListener<MainUseCaseResult>() {

                    @Override
                    public void onStart() {
                        progress.setVisibility(View.VISIBLE);
                    }

                    @Override
                    public void onUpdate(MainUseCaseResult result) {
                        label.setText(result.data);
                    }

                    @Override
                    public void onComplete() {
                        progress.setVisibility(View.GONE);
                    }
                })
                .executeCached();
    }

    private SimpleUseCaseListener<Result> registerUserListener = new SimpleUseCaseListener<Result>() {
        @Override
        public boolean onInputRequired(List<Integer> codes) {
            startActivity(new Intent(getBaseContext(), Registration1Activity.class));
            return true;
        }
    };

    private SimpleUseCaseListener<Result> authenticateLoginListener = new SimpleUseCaseListener<Result>() {

        @Override
        public boolean onInputRequired(List<Integer> codes) {
            if (codes.contains(AuthenticateLogin.PASSWORD))
                startActivity(new Intent(getBaseContext(), LoginActivity.class));
            return true;
        }
    };

    @Override
    public void onBackPressed() {

    }

    @Override
    protected void onStop() {
        super.onStop();

        UseCase.unsubscribe(RegisterUser.class, registerUserListener);
        UseCase.unsubscribe(AuthenticateLogin.class, authenticateLoginListener);
    }
}

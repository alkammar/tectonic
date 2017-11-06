package com.morkim.usecase.ui;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.morkim.tectonic.Result;
import com.morkim.tectonic.SimpleUseCaseListener;
import com.morkim.tectonic.UseCase;
import com.morkim.usecase.R;
import com.morkim.usecase.uc.AuthenticateLogin;
import com.morkim.usecase.uc.MainUseCase;
import com.morkim.usecase.uc.MainUseCaseResult;


public class MainActivity extends AppCompatActivity {

    private TextView label;
    private ProgressBar progress;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.screen_main);

        label = (TextView) findViewById(R.id.label);
        progress = (ProgressBar) findViewById(R.id.prg_progress);

        UseCase.subscribe(AuthenticateLogin.class, authenticateLoginListener);

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

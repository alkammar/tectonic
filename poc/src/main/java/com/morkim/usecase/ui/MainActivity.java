package com.morkim.usecase.ui;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
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

    private Button refresh;
    @SuppressWarnings("FieldCanBeLocal")
    private Button abort;

    private TextView label;
    private ProgressBar progress;
    private SimpleUseCaseListener<Result> logoutListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.screen_main);

        //noinspection ConstantConditions
        getSupportActionBar().setTitle("Home");

        refresh = (Button) findViewById(R.id.btn_refresh);
        abort = (Button) findViewById(R.id.btn_abort);

        label = (TextView) findViewById(R.id.label);
        progress = (ProgressBar) findViewById(R.id.prg_progress);

        // While refreshing you need the use case to overwrite its cached result, so here we use
        // non-cached execution
        refresh.setOnClickListener(v -> UseCase.fetch(MainUseCase.class).execute());

        abort.setOnClickListener(v -> UseCase.cancel(MainUseCase.class));

        UseCase.subscribe(RegisterUser.class, registerUserListener);
        UseCase.subscribe(AuthenticateLogin.class, authenticateLoginListener);

        UseCase.subscribe(ExitApp.class, new SimpleDisposableUseCaseListener<Result>() {
            @Override
            public void onComplete() {
                MainActivity.this.finish();
            }
        });

        findViewById(R.id.btn_logout).setOnClickListener(v -> {
            logoutListener = new SimpleUseCaseListener<Result>() {
                @Override
                public void onComplete() {
                    label.setText("");
                    UseCase.cancel(MainUseCase.class);
                    UseCase.clearCache(MainUseCase.class);
                    startActivity(new Intent(getBaseContext(), LoginActivity.class));
                }
            };
            UseCase.fetch(LogoutUser.class)
                    .subscribe(logoutListener)
                    .execute();
        });
    }

    @Override
    protected void onStart() {
        super.onStart();

        // Execute the main use case cached. If this is our first time to execute it then there will
        // be no cache available in the use case and it will execute normally, otherwise a cached
        // result will be returned
        UseCase.fetch(MainUseCase.class)
                .subscribe(mainUseCaseListener)
                .execute(UseCase.CASHED);
    }

    private SimpleUseCaseListener<MainUseCaseResult> mainUseCaseListener = new SimpleUseCaseListener<MainUseCaseResult>() {

        @Override
        public void onStart() {
            // use case has started
            refresh.setEnabled(false);
            progress.setVisibility(View.VISIBLE);
            Log.i("MainActivity", "onStart");
        }

        @Override
        public void onUpdate(MainUseCaseResult result) {
            // received an update result from the use case
            label.setText(result.data);
            Log.i("MainActivity", "onUpdate: " + result.data);
        }

        @Override
        public void onComplete() {
            // use case has completed
            refresh.setEnabled(true);
            progress.setVisibility(View.GONE);
            Log.i("MainActivity", "onComplete");
        }

        @Override
        public void onCancel() {
            // use case was cancelled
            label.setText(R.string.cancelled);
            refresh.setEnabled(true);
            progress.setVisibility(View.GONE);
            Log.i("MainActivity", "onCancel");
        }
    };

    private SimpleUseCaseListener<Result> registerUserListener = new SimpleUseCaseListener<Result>() {
        @Override
        public boolean onInputRequired(List<Integer> codes) {
            // We received an input required request from the registration use case and we need to
            // navigate to the registration screen
            startActivity(new Intent(getBaseContext(), Registration1Activity.class));

            // Return true to say we handled the input request
            return true;
        }
    };

    private SimpleUseCaseListener<Result> authenticateLoginListener = new SimpleUseCaseListener<Result>() {

        @Override
        public boolean onInputRequired(List<Integer> codes) {
            // We received an input required request from the login use case and we need to navigate
            // to the login screen
            if (codes.contains(AuthenticateLogin.PASSWORD))
                startActivity(new Intent(getBaseContext(), LoginActivity.class));

            // Return true to say we handled the input request
            return true;
        }
    };

    @Override
    public void onBackPressed() {

    }

    @Override
    protected void onStop() {
        super.onStop();

        // Unsubscribe all your non-disposable listeners
        UseCase.unsubscribe(MainUseCase.class, mainUseCaseListener);
        UseCase.unsubscribe(RegisterUser.class, registerUserListener);
        UseCase.unsubscribe(AuthenticateLogin.class, authenticateLoginListener);
        UseCase.unsubscribe(LogoutUser.class, logoutListener);
    }
}

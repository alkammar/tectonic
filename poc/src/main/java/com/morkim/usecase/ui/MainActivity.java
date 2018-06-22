package com.morkim.usecase.ui;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.morkim.tectonic.Result;
import com.morkim.tectonic.SimpleUseCaseListener;
import com.morkim.tectonic.simplified.Triggers;
import com.morkim.usecase.R;
import com.morkim.usecase.actor.User;
import com.morkim.usecase.app.AppTrigger;
import com.morkim.usecase.di.AppInjector;
import com.morkim.usecase.uc.MainUseCaseResult;

import javax.inject.Inject;


public class MainActivity extends AppCompatActivity implements User {

    @Inject
    Triggers<AppTrigger.Event> trigger;

    private Button refresh;
    private Button abort;

    private TextView label;
    private ProgressBar progress;
    private SimpleUseCaseListener<Result> logoutListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        AppInjector.getMainScreenComponent().inject(this);

        setContentView(R.layout.screen_main);

        //noinspection ConstantConditions
        getSupportActionBar().setTitle("Home");

        refresh = findViewById(R.id.btn_refresh);
        abort = findViewById(R.id.btn_abort);

        label = findViewById(R.id.label);
        progress = findViewById(R.id.prg_progress);

        // While refreshing you need the use case to overwrite its cached result, so here we use
        // non-cached execution
        refresh.setOnClickListener(v -> trigger.trigger(AppTrigger.Event.LAUNCH_MAIN));
        trigger.trigger(AppTrigger.Event.LAUNCH_MAIN);
//
//        abort.setOnClickListener(v -> UseCase.cancel(MainUseCase.class));

        findViewById(R.id.btn_logout).setOnClickListener(v -> {
            label.setText("");
            trigger.trigger(AppTrigger.Event.USER_LOGOUT);
        });
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

    @Override
    public void onBackPressed() {

    }
}

package com.morkim.usecase.ui.main;

import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.morkim.tectonic.flow.Step;
import com.morkim.tectonic.flow.StepListener;
import com.morkim.tectonic.usecase.UseCaseHandle;
import com.morkim.tectonic.usecase.Triggers;
import com.morkim.usecase.R;
import com.morkim.usecase.app.UseCaseExecutor;
import com.morkim.usecase.di.AppInjector;
import com.morkim.usecase.di.ui.main.DaggerMainScreenComponent;
import com.morkim.usecase.di.ui.main.MainScreenModule;
import com.morkim.usecase.uc.MainUseCase;

import javax.inject.Inject;


public class MainActivity extends AppCompatActivity implements MainUseCase.UI<UseCaseExecutor.Event> {

    @Inject
    Triggers<UseCaseExecutor.Event> trigger;

    @Inject
    StepListener stepListener;

    private Button refresh;
    private Button abort;

    private TextView label;
    private ProgressBar progress;
    private UseCaseHandle handle;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);


        AppInjector.setMainScreenComponent(
                DaggerMainScreenComponent.builder()
                        .appComponent(AppInjector.getAppComponent())
                        .mainScreenModule(new MainScreenModule(this))
                        .build());

        AppInjector.getMainScreenComponent().inject(this);

        stepListener.onCreated(this);

        setContentView(R.layout.screen_main);

        //noinspection ConstantConditions
        getSupportActionBar().setTitle("Home");

        refresh = findViewById(R.id.btn_refresh);
        abort = findViewById(R.id.btn_abort);

        label = findViewById(R.id.label);
        progress = findViewById(R.id.prg_progress);

        // While refreshing you need the use case to overwrite its cached result, so here we use
        // non-cached execution
        refresh.setOnClickListener(v -> trigger.trigger(UseCaseExecutor.Event.REFRESH_MAIN));


        trigger.trigger(UseCaseExecutor.Event.LAUNCH_MAIN);
//
        abort.setOnClickListener(v -> handle.abort());

        findViewById(R.id.btn_logout).setOnClickListener(v -> {
            label.setText("");
            trigger.trigger(UseCaseExecutor.Event.USER_LOGOUT);
        });

        findViewById(R.id.btn_secondary).setOnClickListener(v -> {
            trigger.trigger(UseCaseExecutor.Event.DO_SECONDARY_THING);
        });
    }

    @Override
    public void onBackPressed() {

    }

    @Override
    public void updateResult(String data) {
        // received an update result from the use case

        runOnUiThread(() -> {
            label.setText(data);
            Log.i("MainActivity", "onUpdate: " + data);
        });
    }

    @Override
    public void onStart(UseCaseExecutor.Event event, UseCaseHandle handle) {
        this.handle = handle;

        runOnUiThread(() -> {
            // use case has started
            refresh.setEnabled(false);
            progress.setVisibility(View.VISIBLE);
            Log.i("MainActivity", "onStart");
        });
    }

    @Override
    public void onComplete(UseCaseExecutor.Event event) {

        runOnUiThread(() -> {
            // use case has completed
            refresh.setEnabled(true);
            progress.setVisibility(View.GONE);
            updateResult("completed");
            Log.i("MainActivity", "onComplete");
        });

    }

    @Override
    public void onUndo(Step step, boolean inclusive) {

    }

    @Override
    public void onAbort(UseCaseExecutor.Event event) {
        runOnUiThread(() -> {
            // use case was cancelled
            label.setText(R.string.cancelled);
            refresh.setEnabled(true);
            progress.setVisibility(View.GONE);
            Log.i("MainActivity", "onAbort");
        });
    }
}

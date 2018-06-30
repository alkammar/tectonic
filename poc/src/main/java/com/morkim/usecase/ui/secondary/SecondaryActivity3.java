package com.morkim.usecase.ui.secondary;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.EditText;
import android.widget.ProgressBar;

import com.morkim.tectonic.flow.StepFactory;
import com.morkim.usecase.R;
import com.morkim.usecase.contract.Secondary;
import com.morkim.usecase.di.AppInjector;
import com.morkim.usecase.di.ui.secondary.DaggerSecondaryScreen3Component;
import com.morkim.usecase.di.ui.secondary.SecondaryScreen3Module;
import com.morkim.usecase.uc.InvalidValueException;
import com.morkim.usecase.uc.SpecificBackendError;

import javax.inject.Inject;


public class SecondaryActivity3 extends AppCompatActivity implements Secondary.Screen3 {

    @Inject
    StepFactory stepFactory;

    @Inject
    Secondary.Flow flow;

    private ProgressBar progress;
    private EditText data3;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        AppInjector.setSecondaryScreen3Component(
                DaggerSecondaryScreen3Component.builder()
                        .appComponent(AppInjector.getAppComponent())
                        .secondaryScreen3Module(new SecondaryScreen3Module(AppInjector.getSecondaryFlowComponent().flow()))
                        .build());

        AppInjector.getSecondaryScreen3Component().inject(this);

        stepFactory.onCreated(this);

        setContentView(R.layout.screen_secondary_last);

        //noinspection ConstantConditions
        getSupportActionBar().setTitle("Secondary Confirm");

        data3 = findViewById(R.id.ti_data1);

        findViewById(R.id.btn_next).setOnClickListener(v -> {
            String s = data3.getText().toString();
            flow.confirm(s.isEmpty() ? 0.00 : Double.parseDouble(s));
        });

        progress = findViewById(R.id.prg_progress);
    }

    @Override
    public void onBackPressed() {
        flow.goBack(this);
    }

    @Override
    public void terminate() {
        finish();
    }

    @Override
    public void block() {
        runOnUiThread(() -> progress.setVisibility(View.VISIBLE));
    }

    @Override
    public void unblock() {
        runOnUiThread(() -> progress.setVisibility(View.GONE));
    }

    @Override
    public void showError(Exception e) {
        if (e instanceof InvalidValueException)
            runOnUiThread(() -> data3.setError("Must enter value > 0"));
        else  if (e instanceof SpecificBackendError)
            runOnUiThread(() -> data3.setError("Value is less than needed!"));

        unblock();
    }
}

package com.morkim.usecase.ui.secondary;

import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import android.widget.EditText;

import com.morkim.tectonic.flow.StepListener;
import com.morkim.usecase.R;
import com.morkim.usecase.contract.Secondary;
import com.morkim.usecase.di.AppInjector;
import com.morkim.usecase.di.ui.secondary.DaggerSecondaryScreen1Component;
import com.morkim.usecase.di.ui.secondary.SecondaryScreen1Module;

import javax.inject.Inject;


public class SecondaryActivity1 extends AppCompatActivity implements Secondary.Screen1 {

    @Inject
    StepListener stepListener;

    @Inject
    Secondary.Flow flow;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        AppInjector.setSecondaryScreen1Component(
                DaggerSecondaryScreen1Component.builder()
                        .appComponent(AppInjector.getAppComponent())
                        .secondaryScreen1Module(new SecondaryScreen1Module(AppInjector.getSecondaryFlowComponent().flow()))
                        .build());

        AppInjector.getSecondaryScreen1Component().inject(this);

        stepListener.onCreated(this);

        setContentView(R.layout.screen_secondary_1);

        //noinspection ConstantConditions
        getSupportActionBar().setTitle("Secondary Origin");

        EditText data1 = findViewById(R.id.ti_data1);

        findViewById(R.id.btn_next).setOnClickListener(v -> flow.submitData1(data1.getText().toString()));
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

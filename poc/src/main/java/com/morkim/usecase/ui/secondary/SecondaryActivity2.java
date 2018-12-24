package com.morkim.usecase.ui.secondary;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.EditText;

import com.morkim.tectonic.flow.StepFactory;
import com.morkim.usecase.R;
import com.morkim.usecase.contract.Secondary;
import com.morkim.usecase.di.AppInjector;
import com.morkim.usecase.di.ui.secondary.DaggerSecondaryScreen2Component;
import com.morkim.usecase.di.ui.secondary.SecondaryScreen2Module;

import javax.inject.Inject;


public class SecondaryActivity2 extends AppCompatActivity implements Secondary.Screen2 {

    @Inject
    StepFactory stepFactory;

    @Inject
    Secondary.Flow flow;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        AppInjector.setSecondaryScreen2Component(
                DaggerSecondaryScreen2Component.builder()
                        .appComponent(AppInjector.getAppComponent())
                        .secondaryScreen2Module(new SecondaryScreen2Module(AppInjector.getSecondaryFlowComponent().flow()))
                        .build());

        AppInjector.getSecondaryScreen2Component().inject(this);

        stepFactory.onCreated(this);

        setContentView(R.layout.screen_secondary_1);

        //noinspection ConstantConditions
        getSupportActionBar().setTitle("Secondary 2");

        EditText data2 = findViewById(R.id.ti_data1);

        findViewById(R.id.btn_next).setOnClickListener(v -> flow.submitData2(data2.getText().toString()));
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

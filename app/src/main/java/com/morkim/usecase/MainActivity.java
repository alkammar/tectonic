package com.morkim.usecase;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import com.morkim.tectonic.Request;
import com.morkim.tectonic.UseCase;

public class MainActivity extends AppCompatActivity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.activity_main);


		try {
			UseCase useCase = new UseCase() {
				@Override
				protected void onExecute(Request request) {

				}
			};
		} catch (Exception e) {

		}
	}
}

package com.morkim.tectonic.entities;


import com.morkim.tectonic.Request;
import com.morkim.tectonic.UseCase;

public class TestUseCase extends UseCase<Request, TestResult> {

	@Override
	protected void onExecute(Request request) {

		TestResult result = new TestResult();

		updateSubscribers(result);

		finish();
	}
}

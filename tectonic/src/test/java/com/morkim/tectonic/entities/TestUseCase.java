package com.morkim.tectonic.entities;


import com.morkim.tectonic.Request;
import com.morkim.tectonic.UseCase;

public class TestUseCase extends UseCase<Request, TestResult> {

	@Override
	protected void onExecute(Request request) throws InterruptedException {

		TestResult result = new TestResult();

		notifySubscribers(result);

		finish();
	}

	@Override
	protected boolean supportsCaching() {
		return false;
	}
}

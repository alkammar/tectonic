package com.morkim.tectonic.entities;


import com.morkim.tectonic.Result;
import com.morkim.tectonic.UseCase;

public class PendingActionTestUseCase extends UseCase<PendingActionRequest, Result> {

	private int onExecuteCount;

	@Override
	protected void onExecute(PendingActionRequest request) {

		onExecuteCount++;

		if (request == null) {
			// Do nothing as if we are waiting for input
		} else {

			TestResult result = new TestResult();

			updateSubscribers(result);

			finish();
		}
	}

	public int getOnExecuteCount() {
		return onExecuteCount;
	}
}

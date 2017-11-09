package com.morkim.tectonic.entities;


import com.morkim.tectonic.Result;
import com.morkim.tectonic.UseCase;

public class UnfinishedTestUseCase extends UseCase<PendingActionRequest, Result> {

	private int onExecuteCount;

	@Override
	protected void onExecute(PendingActionRequest request) {

		onExecuteCount++;
	}

	public int getOnExecuteCount() {
		return onExecuteCount;
	}
}

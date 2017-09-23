package com.morkim.tectonic;

import com.morkim.tectonic.entities.PendingActionRequest;
import com.morkim.tectonic.entities.PendingActionTestUseCase;

import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class MultipleExecutionTest {

	private int useCasesStarted1Count;
	private int useCasesStarted2Count;
	private int onExecuteCount;

	@Before
	public void setup() {

		UseCase.unsubscribeAll();
		UseCase.clearAllInProgress();

		useCasesStarted1Count = 0;
		useCasesStarted2Count = 0;
		onExecuteCount = 0;
	}

	@Test
	public void executeMultiple_onlyNewSubscriptionOnStartAndOnExecute() throws Exception {

		UseCase.subscribe(MainTestUseCase.class, new UseCase.OnStartListener() {
			@Override
			public void onStart() {
				useCasesStarted1Count++;
			}
		});

		MainTestUseCase
		useCase = new MainTestUseCase();
		useCase.execute();

		useCase = new MainTestUseCase();
		useCase.subscribe(new UseCase.OnStartListener() {
			@Override
			public void onStart() {
				useCasesStarted2Count++;
			}
		});
		useCase.execute();

		assertEquals(1, useCasesStarted1Count);
		assertEquals(1, useCasesStarted2Count);
		assertEquals(1, onExecuteCount);
	}

	private class MainTestUseCase extends PendingActionTestUseCase {

		@Override
		protected void onExecute(PendingActionRequest request) {
			super.onExecute(request);

			onExecuteCount++;
		}
	}
}
package com.morkim.tectonic;

import com.morkim.tectonic.entities.PendingActionRequest;
import com.morkim.tectonic.entities.PendingActionTestUseCase;
import com.morkim.tectonic.entities.TestResult;

import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertTrue;

public class InputRequiredExecutionTest {

	private boolean executionContinued;

	@Before
	public void setup() {

		UseCase.unsubscribeAll();
		UseCase.clearAllInProgress();

		executionContinued = false;
	}

	@Test
	public void executeMultiple_onlyNewSubscriptionOnStartAndOnExecute() throws Exception {

		MainTestUseCase
		useCase = new MainTestUseCase();
		useCase.execute();

		useCase = new MainTestUseCase();
		useCase.execute(new PendingActionRequest.Builder().build());

		assertTrue(executionContinued);
	}

	private class MainTestUseCase extends PendingActionTestUseCase {

		@Override
		protected void onExecute(PendingActionRequest request) {

			if (request == null) {
				requestInput(0);
			} else {

				executionContinued = true;

				TestResult result = new TestResult();

				updateSubscribers(result);

				finish();
			}
		}
	}
}
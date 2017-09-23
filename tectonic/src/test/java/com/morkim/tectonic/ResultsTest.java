package com.morkim.tectonic;

import com.morkim.tectonic.entities.TestResult;
import com.morkim.tectonic.entities.TestUseCase;

import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

public class ResultsTest {

	private TestResult updateResult;
	private int resultCount;

	@Before
	public void setup() {

		updateResult = null;
		resultCount = 0;
	}

	@Test
	public void execute_resultReturned() throws Exception {

		TestUseCase useCase = new TestUseCase();
		useCase.subscribe(new UseCase.OnUpdateListener<TestResult>() {
			@Override
			public void onUpdate(TestResult result) {
				updateResult = result;
				resultCount++;
			}
		});

		useCase.execute();

		assertNotNull(updateResult);
		assertEquals(1, resultCount);
	}

	@Test
	public void executeMultipleResults_multipleResultsReturned() throws Exception {

		MultipleResultsUseCase useCase = new MultipleResultsUseCase();
		useCase.subscribe(new UseCase.OnUpdateListener<TestResult>() {
			@Override
			public void onUpdate(TestResult result) {
				updateResult = result;
				resultCount++;
			}
		});

		useCase.execute();

		assertNotNull(updateResult);
		assertEquals(2, resultCount);
	}

	private class MultipleResultsUseCase extends TestUseCase {

		@Override
		protected void onExecute(Request request) {

			TestResult result;

			result = new TestResult();
			updateSubscribers(result);

			result = new TestResult();
			updateSubscribers(result);
		}
	}
}
package com.morkim.tectonic;

import com.morkim.tectonic.entities.PrerequisiteTestUseCase;
import com.morkim.tectonic.entities.TestResult;
import com.morkim.tectonic.entities.TestUseCase;

import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class SubscriptionTest {

	private boolean isSubscriptionExecuted;

	@Before
	public void setup() {

		isSubscriptionExecuted = false;

		UseCase.unsubscribeAll();
	}

	@Test
	public void subscribeThenExecute_SubscribedCallbacksExecuted() throws Exception {

		UseCase.subscribe(TestUseCase.class, new UseCase.OnUpdateListener<TestResult>() {
			@Override
			public void onUpdate(TestResult result) {
				isSubscriptionExecuted = true;
			}
		});

		TestUseCase useCase;

		useCase = new TestUseCase();
		useCase.execute();

		assertTrue(isSubscriptionExecuted);
	}

	@Test
	public void unsubscribeThenExecute_NoCallbacksExecuted() throws Exception {

		UseCase.OnUpdateListener<TestResult> listener = new UseCase.OnUpdateListener<TestResult>() {
			@Override
			public void onUpdate(TestResult result) {
				isSubscriptionExecuted = true;
			}
		};

		UseCase.subscribe(TestUseCase.class, listener);

		UseCase.unsubscribe(TestUseCase.class, listener);

		TestUseCase useCase;

		useCase = new TestUseCase();
		useCase.execute();

		assertFalse(isSubscriptionExecuted);
	}

	@Test
	public void unsubscribeAllForUseCaseThenExecute_NoCallbacksExecuted() throws Exception {

		UseCase.OnUpdateListener<TestResult> listener = new UseCase.OnUpdateListener<TestResult>() {
			@Override
			public void onUpdate(TestResult result) {
				isSubscriptionExecuted = true;
			}
		};

		UseCase.subscribe(TestUseCase.class, listener);

		UseCase.unsubscribe(TestUseCase.class);

		TestUseCase useCase;

		useCase = new TestUseCase();
		useCase.execute();

		assertFalse(isSubscriptionExecuted);
	}

	@Test
	public void unsubscribeOtherUseCaseThenExecute_SubscribedCallbacksExecuted() throws Exception {

		UseCase.OnUpdateListener<TestResult> listener = new UseCase.OnUpdateListener<TestResult>() {
			@Override
			public void onUpdate(TestResult result) {
				isSubscriptionExecuted = true;
			}
		};

		UseCase.subscribe(TestUseCase.class, listener);

		UseCase.unsubscribe(PrerequisiteTestUseCase.class, listener);

		TestUseCase useCase;

		useCase = new TestUseCase();
		useCase.execute();

		assertTrue(isSubscriptionExecuted);
	}

}
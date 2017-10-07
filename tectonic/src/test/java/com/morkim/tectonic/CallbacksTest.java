package com.morkim.tectonic;

import android.support.annotation.NonNull;

import com.morkim.tectonic.entities.PendingActionRequest;
import com.morkim.tectonic.entities.TestResult;
import com.morkim.tectonic.entities.TestUseCase;

import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import java.util.concurrent.Callable;

import io.reactivex.Scheduler;
import io.reactivex.android.plugins.RxAndroidPlugins;
import io.reactivex.functions.Function;
import io.reactivex.plugins.RxJavaPlugins;
import io.reactivex.schedulers.Schedulers;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class CallbacksTest extends TecTonicTest {

	private int isOnStartCalledCount;
	private int isOnUpdateCalledCount;
	private int isOnCompleteCalledCount;
	private boolean isOnExecuteCalled;
	private boolean isOnPostExecuteCalled;

	@BeforeClass
	public static void setupClass() {

		RxAndroidPlugins.setInitMainThreadSchedulerHandler(new Function<Callable<Scheduler>, Scheduler>() {
			@Override
			public Scheduler apply(@NonNull Callable<Scheduler> schedulerCallable) throws Exception {
				return Schedulers.trampoline();
			}
		});

		RxJavaPlugins.setIoSchedulerHandler(new Function<Scheduler, Scheduler>() {
			@Override
			public Scheduler apply(@io.reactivex.annotations.NonNull Scheduler scheduler) throws Exception {
				return Schedulers.trampoline();
			}
		});
	}

	@Before
	public void setup() {

		isOnStartCalledCount = 0;
		isOnUpdateCalledCount = 0;
		isOnCompleteCalledCount = 0;
		isOnExecuteCalled = false;
		isOnPostExecuteCalled = false;
	}

	@Test
	public void executeNoListener_callbacksNotCalled() throws Exception {

		UseCase useCase = new MainTestUseCase();

		useCase.execute();

		assertEquals(0, isOnStartCalledCount);
		assertEquals(0, isOnUpdateCalledCount);
		assertEquals(0, isOnCompleteCalledCount);
	}

	@Test
	public void execute_callbacksCalled() throws Exception {

		TestUseCase useCase = new TestUseCase();
		useCase.subscribe(createCallbackCounterListener());

		useCase.execute();

		assertEquals(1, isOnStartCalledCount);
		assertEquals(1, isOnUpdateCalledCount);
		assertEquals(1, isOnCompleteCalledCount);
	}

	@Test
	public void executeMultipleSubscription_callbacksCalled() throws Exception {

		TestUseCase useCase = new TestUseCase();
		useCase.subscribe(createCallbackCounterListener());
		UseCase.subscribe(TestUseCase.class, createCallbackCounterListener());
		UseCase.subscribe(TestUseCase.class, createCallbackCounterListener());

		useCase.execute();

		assertEquals(3, isOnStartCalledCount);
		assertEquals(3, isOnUpdateCalledCount);
		assertEquals(3, isOnCompleteCalledCount);
	}

	@Test
	public void executeMultipleSubscriptionWithUnsubscribe_callbacksCalled() throws Exception {

		TestUseCase useCase = new TestUseCase();
		useCase.subscribe(createCallbackCounterListener());
		UseCaseListener<TestResult> callbackCounterListener = createCallbackCounterListener();
		UseCase.subscribe(TestUseCase.class, callbackCounterListener);
		useCase.unsubscribe(callbackCounterListener);
		UseCase.subscribe(TestUseCase.class, createCallbackCounterListener());

		useCase.execute();

		assertEquals(2, isOnStartCalledCount);
		assertEquals(2, isOnUpdateCalledCount);
		assertEquals(2, isOnCompleteCalledCount);
	}

	@Test
	public void executeAllUnsubscribed_callbacksNotCalled() throws Exception {

		TestUseCase useCase = new TestUseCase();
		UseCaseListener<TestResult> callbackCounterListener1 = createCallbackCounterListener();
		useCase.subscribe(callbackCounterListener1);
		UseCaseListener<TestResult> callbackCounterListener2 = createCallbackCounterListener();
		useCase.subscribe(callbackCounterListener2);
		UseCaseListener<TestResult> callbackCounterListener3 = createCallbackCounterListener();
		useCase.subscribe(callbackCounterListener3);

		useCase.unsubscribe(callbackCounterListener1);
		useCase.unsubscribe(callbackCounterListener2);
		useCase.unsubscribe(callbackCounterListener3);

		useCase.execute();

		assertEquals(0, isOnStartCalledCount);
		assertEquals(0, isOnUpdateCalledCount);
		assertEquals(0, isOnCompleteCalledCount);
	}

	@Test
	public void executeUnsubscribeNonSubscriber_correctCountCalled() throws Exception {

		TestUseCase useCase = new TestUseCase();
		UseCaseListener<TestResult> callbackCounterListener1 = createCallbackCounterListener();
		useCase.subscribe(callbackCounterListener1);
		UseCaseListener<TestResult> callbackCounterListener2 = createCallbackCounterListener();
		UseCase.subscribe(TestUseCase.class, callbackCounterListener2);
		UseCaseListener<TestResult> callbackCounterListener3 = createCallbackCounterListener();
		UseCase.subscribe(TestUseCase.class, callbackCounterListener3);
		UseCaseListener<TestResult> callbackCounterListener4 = createCallbackCounterListener();

		UseCase.unsubscribe(TestUseCase.class, callbackCounterListener4);

		useCase.execute();

		assertEquals(3, isOnStartCalledCount);
		assertEquals(3, isOnUpdateCalledCount);
		assertEquals(3, isOnCompleteCalledCount);
	}

	@Test
	public void executeWithFinish_onCompleteCalled() throws Exception {

		TestUseCase useCase = new TestUseCase();
		useCase.subscribe(createCallbackCounterListener());

		useCase.execute();

		assertEquals(1, isOnCompleteCalledCount);
	}

	@Test
	public void executeWithoutFinish_onCompleteNotCalled() throws Exception {

		TestUseCase useCase = new TestUseCase() {
			@Override
			protected void onExecute(Request request) {

				TestResult result = new TestResult();

				updateSubscribers(result);
			}
		};
		useCase.subscribe(createCallbackCounterListener());

		useCase.execute();

		assertEquals(0, isOnCompleteCalledCount);
	}

	@Test
	public void executeNoSubscribers_onExecuteCalled() throws Exception {

		TestUseCase useCase = new TestUseCase() {

			@Override
			protected void onExecute(Request request) {

				isOnExecuteCalled = true;
			}
		};

		useCase.execute();

		assertTrue(isOnExecuteCalled);
	}

	@Test
	public void executeNoSubscribers_onPostExecuteCalledAfterComplete() throws Exception {

		MainTestUseCase useCase = new MainTestUseCase() {

			@Override
			protected void onPostExecute() {

				isOnPostExecuteCalled = true;
			}
		};

		useCase.execute();

		assertTrue(isOnPostExecuteCalled);
	}

	@NonNull
	private UseCaseListener<TestResult> createCallbackCounterListener() {
		return new SimpleUseCaseListener<TestResult>() {

			@Override
			public void onStart() {
				isOnStartCalledCount++;
			}

			@Override
			public void onUpdate(TestResult result) {
				isOnUpdateCalledCount++;
			}

			@Override
			public void onComplete() {
				isOnCompleteCalledCount++;
			}
		};
	}

	private class MainTestUseCase extends UseCase<PendingActionRequest, TestResult> {

		@Override
		protected void onExecute(PendingActionRequest request) {


			TestResult result = new TestResult();

			updateSubscribers(result);

			finish();
		}
	}

}
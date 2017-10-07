package com.morkim.tectonic;

import android.support.annotation.NonNull;

import com.morkim.tectonic.entities.PendingActionRequest;
import com.morkim.tectonic.entities.PendingActionTestUseCase;
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

public class MultipleExecutionTest extends TecTonicTest {

	private int useCasesStarted1Count;
	private int useCasesStarted2Count;
	private int onExecuteCount;

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

		UseCase.unsubscribeAll();
		UseCase.clearAllInProgress();

		useCasesStarted1Count = 0;
		useCasesStarted2Count = 0;
		onExecuteCount = 0;
	}

	@Test
	public void executeMultiple_onlyNewSubscriptionOnStartAndOnExecute() throws Exception {

		UseCase.subscribe(MainTestUseCase.class, new SimpleUseCaseListener<Result>() {
			@Override
			public void onStart() {
				useCasesStarted1Count++;
			}
		});

		MainTestUseCase
		useCase = new MainTestUseCase();
		useCase.execute();

		useCase = new MainTestUseCase();
		useCase.subscribe(new SimpleUseCaseListener<Result>() {
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

	@Test
	public void executeAfterComplete_AllSubscribersCalled() throws Exception {

		UseCase.subscribe(TestUseCase.class, new SimpleUseCaseListener<Result>() {
			@Override
			public void onStart() {
				useCasesStarted1Count++;
			}
		});

		TestUseCase
		useCase = new TestUseCase();
		useCase.execute();

		useCase = new TestUseCase();
		useCase.subscribe(new SimpleUseCaseListener<TestResult>() {
			@Override
			public void onStart() {
				useCasesStarted2Count++;
			}
		});
		useCase.execute();

		assertEquals(2, useCasesStarted1Count);
		assertEquals(1, useCasesStarted2Count);
	}

	private class MainTestUseCase extends PendingActionTestUseCase {

		@Override
		protected void onExecute(PendingActionRequest request) {
			super.onExecute(request);

			onExecuteCount++;
		}
	}
}
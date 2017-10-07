package com.morkim.tectonic;

import android.support.annotation.NonNull;

import com.morkim.tectonic.entities.PendingActionRequest;
import com.morkim.tectonic.entities.PendingActionTestUseCase;
import com.morkim.tectonic.entities.TestResult;

import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import java.util.concurrent.Callable;

import io.reactivex.Scheduler;
import io.reactivex.android.plugins.RxAndroidPlugins;
import io.reactivex.functions.Function;
import io.reactivex.plugins.RxJavaPlugins;
import io.reactivex.schedulers.Schedulers;

import static org.junit.Assert.assertTrue;

public class InputRequiredExecutionTest extends TecTonicTest {

	private boolean executionContinued;

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
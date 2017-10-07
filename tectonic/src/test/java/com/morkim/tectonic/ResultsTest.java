package com.morkim.tectonic;

import android.support.annotation.NonNull;

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
import static org.junit.Assert.assertNotNull;

public class ResultsTest extends TecTonicTest {

	private TestResult updateResult;
	private int resultCount;

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

		updateResult = null;
		resultCount = 0;
	}

	@Test
	public void execute_resultReturned() throws Exception {

		TestUseCase useCase = new TestUseCase();
		useCase.subscribe(new SimpleUseCaseListener<TestResult>() {
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
		useCase.subscribe(new SimpleUseCaseListener<TestResult>() {
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
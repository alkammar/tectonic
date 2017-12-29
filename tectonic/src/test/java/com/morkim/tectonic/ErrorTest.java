package com.morkim.tectonic;

import android.support.annotation.NonNull;

import com.morkim.tectonic.entities.ErrorPrerequisiteTestUseCase;
import com.morkim.tectonic.entities.ErrorTestUseCase;
import com.morkim.tectonic.entities.ErrorWithPrerequisiteTestUseCase;
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

import static junit.framework.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;

public class ErrorTest extends TecTonicTest {

	private int onErrorCalled;
	private long mainTimeStamp;

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

		UseCase.setLooperConfigs(UseCase.STUB_LOOPER_CHECKER);
	}

	@Before
	public void setup() {

		UseCase.unsubscribeAll();

		onErrorCalled = 0;
		mainTimeStamp = 0;
	}

	@Test
	public void execute_errorCallbackCalled() throws Exception {

		TestUseCase useCase;

		useCase = UseCase.fetch(ErrorTestUseCase.class);
		useCase.subscribe(new SimpleUseCaseListener<TestResult>() {
			@Override
			public boolean onError(Throwable throwable) {
				onErrorCalled++;
				return true;
			}
		});
		useCase.execute();

		assertEquals(1, onErrorCalled);
	}

	@Test
	public void prerequisiteError_mainInProgress() throws Exception {

		ErrorPrerequisiteTestUseCase useCase;

		useCase = UseCase.fetch(ErrorPrerequisiteTestUseCase.class);
		useCase.subscribe(new SimpleUseCaseListener<TestResult>() {
			@Override
			public void onComplete() {
				mainTimeStamp = System.nanoTime();
			}
		});
		useCase.execute();

		assertNotEquals(0, useCase.getPrerequisiteTimeStamp());
		assertNotEquals(0, mainTimeStamp);
	}

	@Test
	public void mainErrorWithPrerequisiteComplete_mainError() throws Exception {

		ErrorWithPrerequisiteTestUseCase useCase;

		useCase = UseCase.fetch(ErrorWithPrerequisiteTestUseCase.class);
		useCase.subscribe(new SimpleUseCaseListener<TestResult>() {
			@Override
			public boolean onError(Throwable throwable) {
				onErrorCalled++;
				return true;
			}
		});
		useCase.execute();

		assertEquals(1, onErrorCalled);
	}
}
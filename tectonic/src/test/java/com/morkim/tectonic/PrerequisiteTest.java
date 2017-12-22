package com.morkim.tectonic;

import android.support.annotation.NonNull;

import com.morkim.tectonic.entities.HasPrerequisitesTestUseCase;
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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertTrue;

public class PrerequisiteTest extends TecTonicTest {

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

		mainTimeStamp = 0;
	}

	@Test
	public void execute_prerequisitesExecuted() throws Exception {

		HasPrerequisitesTestUseCase useCase;

		useCase = UseCase.fetch(HasPrerequisitesTestUseCase.class);
		useCase.subscribe(new SimpleUseCaseListener<TestResult>() {
			@Override
			public void onComplete() {
				mainTimeStamp = System.nanoTime();
			}
		});
		useCase.execute();

		assertNotEquals(0, useCase.getPrerequisite1TimeStamp());
		assertEquals(0, useCase.getPrerequisite2TimeStamp());
		assertNotEquals(0, useCase.getPrerequisite3TimeStamp());
		assertNotEquals(0, mainTimeStamp);
		assertTrue(useCase.getPrerequisite1TimeStamp() < mainTimeStamp);
		assertTrue(useCase.getPrerequisite1TimeStamp() < useCase.getPrerequisite3TimeStamp());
		assertTrue(useCase.getPrerequisite3TimeStamp() < mainTimeStamp);
	}

	@Test
	public void reexecute_prerequisitesExecuted() throws Exception {

		HasPrerequisitesTestUseCase useCase;

		useCase = UseCase.fetch(HasPrerequisitesTestUseCase.class);
		useCase.subscribe(new SimpleUseCaseListener<TestResult>() {
			@Override
			public void onComplete() {
				mainTimeStamp = System.nanoTime();
			}
		}).execute();

		mainTimeStamp = 0;

		useCase = UseCase.fetch(HasPrerequisitesTestUseCase.class);
		useCase.execute();

		assertNotEquals(0, useCase.getPrerequisite1TimeStamp());
		assertEquals(0, useCase.getPrerequisite2TimeStamp());
		assertNotEquals(0, useCase.getPrerequisite3TimeStamp());
		assertNotEquals(0, mainTimeStamp);
		assertTrue(useCase.getPrerequisite1TimeStamp() < mainTimeStamp);
		assertTrue(useCase.getPrerequisite1TimeStamp() < useCase.getPrerequisite3TimeStamp());
		assertTrue(useCase.getPrerequisite3TimeStamp() < mainTimeStamp);
	}

}
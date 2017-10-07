package com.morkim.tectonic;

import android.support.annotation.NonNull;

import com.morkim.tectonic.entities.PrerequisiteTestUseCase;
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
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertTrue;

public class PrerequisiteTest extends TecTonicTest {

	private long prerequisite1TimeStamp;
	private long prerequisite2TimeStamp;
	private long prerequisite3TimeStamp;
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
	}

	@Before
	public void setup() {

		prerequisite1TimeStamp = 0;
		prerequisite2TimeStamp = 0;
		prerequisite3TimeStamp = 0;
		mainTimeStamp = 0;
	}

	@Test
	public void execute_prerequisitesExecuted() throws Exception {

		TestUseCase useCase;

		useCase = new MainTestUseCase();
		useCase.subscribe(new SimpleUseCaseListener<TestResult>() {
			@Override
			public void onComplete() {
				mainTimeStamp = System.nanoTime();
			}
		});
		useCase.execute();

		assertNotEquals(0, prerequisite1TimeStamp);
		assertEquals(0, prerequisite2TimeStamp);
		assertNotEquals(0, prerequisite3TimeStamp);
		assertNotEquals(0, mainTimeStamp);
		assertTrue(prerequisite1TimeStamp < mainTimeStamp);
		assertTrue(prerequisite1TimeStamp < prerequisite3TimeStamp);
		assertTrue(prerequisite3TimeStamp < mainTimeStamp);
	}

	@Test
	public void reexecute_prerequisitesExecuted() throws Exception {

		TestUseCase useCase;

		useCase = new MainTestUseCase();
		useCase.subscribe(new SimpleUseCaseListener<TestResult>() {
			@Override
			public void onComplete() {
				mainTimeStamp = System.nanoTime();
			}
		}).execute();

		prerequisite1TimeStamp = 0;
		prerequisite2TimeStamp = 0;
		prerequisite3TimeStamp = 0;
		mainTimeStamp = 0;

		useCase.execute();

		assertNotEquals(0, prerequisite1TimeStamp);
		assertEquals(0, prerequisite2TimeStamp);
		assertNotEquals(0, prerequisite3TimeStamp);
		assertNotEquals(0, mainTimeStamp);
		assertTrue(prerequisite1TimeStamp < mainTimeStamp);
		assertTrue(prerequisite1TimeStamp < prerequisite3TimeStamp);
		assertTrue(prerequisite3TimeStamp < mainTimeStamp);
	}

	private class MainTestUseCase extends TestUseCase {

		@Override
		protected void onAddPrerequisites() {

			addPrerequisite(
					PrerequisiteTestUseCase.class,
					new SimpleUseCaseListener<TestResult>() {
						@Override
						public void onComplete() {
							prerequisite1TimeStamp = System.nanoTime();
						}
					});

			addPrerequisite(
					TestUseCase.class,
					false,
					new SimpleUseCaseListener<TestResult>() {
						@Override
						public void onComplete() {
							prerequisite2TimeStamp = System.nanoTime();
						}
					});

			addPrerequisite(
					TestUseCase.class,
					new SimpleUseCaseListener<TestResult>() {
						@Override
						public void onComplete() {
							prerequisite3TimeStamp = System.nanoTime();
						}
					});
		}
	}

}
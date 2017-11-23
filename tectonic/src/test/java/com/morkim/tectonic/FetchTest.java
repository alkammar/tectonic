package com.morkim.tectonic;

import android.support.annotation.NonNull;

import com.morkim.tectonic.entities.CreatedTestUseCase;
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

public class FetchTest extends TecTonicTest {

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
	}

	@Test
	public void fetchCreatedUseCase_returnsOldUseCase() throws Exception {

		CreatedTestUseCase useCase = UseCase.fetch(CreatedTestUseCase.class);
		useCase.execute();

		CreatedTestUseCase useCase2 = UseCase.fetch(CreatedTestUseCase.class);

		assertEquals(useCase, useCase2);
	}

	@Test
	public void fetchCompletedUseCase_returnsNewUseCase() throws Exception {

		TestUseCase useCase = UseCase.fetch(TestUseCase.class);
		useCase.execute();

		TestUseCase useCase2 = UseCase.fetch(TestUseCase.class);

		assertNotEquals(useCase, useCase2);
	}

}
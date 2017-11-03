package com.morkim.tectonic;

import android.support.annotation.NonNull;

import com.morkim.tectonic.entities.CachableTestUseCase;
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

public class CacheTest extends TecTonicTest {

	private Result originalResult;
	private Result cachedResult;

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

		originalResult = null;
		cachedResult = null;
	}

	@Test
	public void executeWithoutCaching_returnNewData() throws Exception {

		TestUseCase useCase;

		useCase = UseCase.fetch(CachableTestUseCase.class);
		SimpleUseCaseListener<TestResult> originalResultListener = createOriginalResultListener();
		useCase.subscribe(originalResultListener);
		useCase.execute();

		useCase.unsubscribe(originalResultListener);

		useCase = UseCase.fetch(CachableTestUseCase.class);
		useCase.subscribe(createCachedResultListener());
		useCase.execute();

		assertNotEquals(originalResult, cachedResult);
	}

	@Test
	public void executeNonCachable_returnNewData() throws Exception {

		TestUseCase useCase;

		useCase = UseCase.fetch(TestUseCase.class);
		SimpleUseCaseListener<TestResult> originalResultListener = createOriginalResultListener();
		useCase.subscribe(originalResultListener);
		useCase.execute();

		useCase.unsubscribe(originalResultListener);

		useCase = new TestUseCase();
		useCase.subscribe(createCachedResultListener());
		useCase.executeCached();

		assertNotEquals(originalResult, cachedResult);
	}

	@Test
	public void executeSecondTime_returnCachedData() throws Exception {

		TestUseCase useCase;

		useCase = UseCase.fetch(CachableTestUseCase.class);
		useCase.subscribe(createOriginalResultListener());
		useCase.execute();

		useCase = UseCase.fetch(CachableTestUseCase.class);
		useCase.subscribe(createCachedResultListener());
		useCase.executeCached();

		assertEquals(originalResult, cachedResult);
	}

	@Test
	public void executeAfterClear_returnNewData() throws Exception {

		TestUseCase useCase;

		useCase = UseCase.fetch(CachableTestUseCase.class);
		SimpleUseCaseListener<TestResult> originalResultListener = createOriginalResultListener();
		useCase.subscribe(originalResultListener);
		useCase.execute();

		UseCase.clearCache(CachableTestUseCase.class);
		useCase.unsubscribe(originalResultListener);

		useCase = UseCase.fetch(CachableTestUseCase.class);
		useCase.subscribe(createCachedResultListener());
		useCase.executeCached();

		assertNotEquals(originalResult, cachedResult);
	}

	@NonNull
	private SimpleUseCaseListener<TestResult> createOriginalResultListener() {
		return new SimpleUseCaseListener<TestResult>() {
			@Override
			public void onUpdate(TestResult result) {
				originalResult = result;
			}
		};
	}

	@NonNull
	private SimpleUseCaseListener<TestResult> createCachedResultListener() {
		return new SimpleUseCaseListener<TestResult>() {
			@Override
			public void onUpdate(TestResult result) {
				cachedResult = result;
			}
		};
	}
}
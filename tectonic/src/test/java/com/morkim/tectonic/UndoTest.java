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

public class UndoTest extends TecTonicTest {

	private Result originalResult;
	private Result cachedResult;

	private int onStartCachedCalled;
	private int onCompleteCachedCalled;
	private int onUndoneCalled;

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
		UseCase.clearCache(CachableTestUseCase.class);

		originalResult = null;
		cachedResult = null;
		onStartCachedCalled = 0;
		onCompleteCachedCalled = 0;
		onUndoneCalled = 0;
	}

	@Test
	public void undoWithoutEarlierExecution_onUndoNotCalled() throws Exception {

		UseCase.fetch(CachableTestUseCase.class)
				.subscribe(new SimpleUseCaseListener<TestResult>() {
					@Override
					public void onUndone() {
						onUndoneCalled++;
					}
				})
				.undo();

		assertEquals(0, onUndoneCalled);
	}

	@Test
	public void undoWithoutCaching_onUndoNotCalled() throws Exception {

		UseCase.fetch(TestUseCase.class)
				.subscribe(new SimpleUseCaseListener<TestResult>() {
					@Override
					public void onUndone() {
						onUndoneCalled++;
					}
				})
				.execute();

		UseCase.fetch(TestUseCase.class)
				.undo();

		assertEquals(0, onUndoneCalled);
	}

	@Test
	public void undoExecutedCached_undoneCalled() throws Exception {

		UseCase.fetch(CachableTestUseCase.class)
				.subscribe(new SimpleUseCaseListener<TestResult>() {
					@Override
					public void onUndone() {
						onUndoneCalled++;
					}
				})
				.execute(UseCase.CASHED);

		UseCase.fetch(CachableTestUseCase.class)
				.undo();

		assertEquals(1, onUndoneCalled);
	}

	@Test
	public void executeSecondTime_returnCachedData() throws Exception {

		TestUseCase useCase;

		useCase = UseCase.fetch(CachableTestUseCase.class);
		useCase.subscribe(createOriginalResultListener());
		useCase.execute();

		useCase = UseCase.fetch(CachableTestUseCase.class);
		useCase.subscribe(createCachedResultListener());
		useCase.execute(UseCase.CASHED);

		assertEquals(originalResult, cachedResult);
		assertEquals(1, onStartCachedCalled);
		assertEquals(1, onCompleteCachedCalled);
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
		useCase.execute(UseCase.CASHED);

		assertNotEquals(originalResult, cachedResult);
		assertEquals(1, onStartCachedCalled);
		assertEquals(1, onCompleteCachedCalled);
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
			public void onStart() {
				onStartCachedCalled++;
			}

			@Override
			public void onUpdate(TestResult result) {
				cachedResult = result;
			}

			@Override
			public void onComplete() {
				onCompleteCachedCalled++;
			}
		};
	}
}
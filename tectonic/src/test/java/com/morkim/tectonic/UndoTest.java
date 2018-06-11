package com.morkim.tectonic;

import android.support.annotation.NonNull;

import com.morkim.tectonic.entities.CacheableTestUseCase;
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

public class UndoTest extends TecTonicTest {

	private int onUndoneCalled;
	private Result oldResult;
	private Result originalResult;

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
		UseCase.clearCache(CacheableTestUseCase.class);

		onUndoneCalled = 0;
		originalResult = null;
		oldResult = null;
	}

	@Test
	public void undoWithoutEarlierExecution_onUndoNotCalled() throws Exception {

		UseCase.fetch(CacheableTestUseCase.class)
				.subscribe(new SimpleUseCaseListener<TestResult>() {
					@Override
					public void onUndone(TestResult oldResult) {
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
					public void onUndone(TestResult oldResult) {
						onUndoneCalled++;
					}
				})
				.execute();

		UseCase.fetch(TestUseCase.class)
				.undo();

		assertEquals(0, onUndoneCalled);
	}

	@Test
	public void undoExecutedCached_onUndoCalled() throws Exception {

		UseCase.fetch(CacheableTestUseCase.class)
				.subscribe(new SimpleUseCaseListener<TestResult>() {

					@Override
					public void onUpdate(TestResult result) {
						UndoTest.this.originalResult = result;
					}

					@Override
					public void onUndone(TestResult oldResult) {
						UndoTest.this.oldResult = oldResult;
						onUndoneCalled++;
					}
				})
				.execute(UseCase.CACHED);

		UseCase.fetch(CacheableTestUseCase.class)
				.undo();

		assertEquals(1, onUndoneCalled);
		assertEquals(originalResult, oldResult);
	}

	@Test
	public void undoAlreadyUndone_onUndoNotCalledSecondTime() throws Exception {

		UseCase.fetch(CacheableTestUseCase.class)
				.subscribe(new SimpleUseCaseListener<TestResult>() {

					@Override
					public void onUndone(TestResult oldResult) {
						onUndoneCalled++;
					}
				})
				.execute(UseCase.CACHED);

		UseCase.fetch(CacheableTestUseCase.class)
				.undo();

		UseCase.fetch(CacheableTestUseCase.class)
				.undo();

		assertEquals(1, onUndoneCalled);
	}
}
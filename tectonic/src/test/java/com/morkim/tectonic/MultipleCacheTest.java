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
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNotNull;

public class MultipleCacheTest extends TecTonicTest {

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

		useCase = new CachableTestUseCase();
		useCase.subscribe(createOriginalResultListener());
		useCase.execute(new CashableRequest.Builder()
				.param1(65)
				.build());

		useCase = new CachableTestUseCase();
		useCase.subscribe(createCachedResultListener());
		useCase.execute(new CashableRequest.Builder()
				.param1(65)
				.build());

		assertNotEquals(originalResult, cachedResult);
	}

	@Test
	public void executeNonCachable_returnNewData() throws Exception {

		TestUseCase useCase;

		useCase = new TestUseCase();
		useCase.subscribe(createOriginalResultListener());
		useCase.execute(new CashableRequest.Builder()
				.param1(65)
				.build());

		useCase = new TestUseCase();
		useCase.subscribe(createCachedResultListener());
		useCase.executeCached(new CashableRequest.Builder()
				.param1(65)
				.build());

		assertNotEquals(originalResult, cachedResult);
	}

	@Test
	public void executeDifferentRequests_returnNewData() throws Exception {

		TestUseCase useCase;

		useCase = new CachableTestUseCase();
		useCase.subscribe(createOriginalResultListener());
		useCase.execute(new CashableRequest.Builder()
				.param1(65)
				.build());

		useCase = new CachableTestUseCase();
		useCase.subscribe(createCachedResultListener());
		useCase.executeCached(new CashableRequest.Builder()
				.param1(77)
				.build());

		assertNotEquals(originalResult, cachedResult);
	}

	@Test
	public void executeSameRequest_returnCachedData() throws Exception {

		TestUseCase useCase;

		useCase = new CachableTestUseCase();
		useCase.subscribe(createOriginalResultListener());
		useCase.execute(new CashableRequest.Builder()
				.param1(65)
				.build());

		useCase = new CachableTestUseCase();
		useCase.subscribe(createCachedResultListener());
		useCase.executeCached(new CashableRequest.Builder()
				.param1(65)
				.build());

		assertEquals(originalResult, cachedResult);
	}

	@Test
	public void executeNewRequest_returnNewData() throws Exception {

		TestUseCase useCase;

		useCase = new CachableTestUseCase();
		useCase.subscribe(createOriginalResultListener());
		useCase.execute(new CashableRequest.Builder()
				.param1(65)
				.build());

		useCase = new CachableTestUseCase();
		useCase.subscribe(createCachedResultListener());
		useCase.executeCached(new CashableRequest.Builder()
				.param1(66)
				.build());

		assertNotEquals(originalResult, cachedResult);
		assertNotNull(cachedResult);
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

	private class CachableTestUseCase extends TestUseCase {

		@Override
		protected boolean isCachable() {
			return true;
		}
	}

	private static class CashableRequest extends Request {

		private final int param1;

		public static class Builder extends Request.Builder<Builder> {

			private int param1 = 0;

			public Builder param1(int val) {
				param1 = val;
				return this;
			}

			public CashableRequest build() { return new CashableRequest(this); }
		}

		protected CashableRequest(Builder builder) {
			super(builder);
			param1 = builder.param1;
		}

		@Override
		protected int id() {
			return param1;
		}
	}
}
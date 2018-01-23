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
import static org.junit.Assert.assertNotNull;

public class MultipleCacheTest {

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

		UseCase.setLooperConfigs(UseCase.STUB_LOOPER_CHECKER);
	}

	@Before
	public void setup() {

		UseCase.unsubscribeAll();

		originalResult = null;
		cachedResult = null;
	}

	@Test
	public void executeWithoutCaching_returnNewData() throws Exception {

		CachableTestUseCase useCase;

		useCase = UseCase.fetch(CachableTestUseCase.class);
		useCase.subscribe(createOriginalResultListener());
		useCase.execute(new CachableRequest.Builder()
				.param1(65)
				.build());

		UseCase.unsubscribe(CachableTestUseCase.class);

		useCase = UseCase.fetch(CachableTestUseCase.class);
		useCase.subscribe(createCachedResultListener());
		useCase.execute(new CachableRequest.Builder()
				.param1(65)
				.build());

		assertNotEquals(originalResult, cachedResult);
	}

	@Test
	public void executeNonCachable_returnNewData() throws Exception {

		TestUseCase useCase;

		useCase = UseCase.fetch(TestUseCase.class);
		useCase.subscribe(createOriginalResultListener());
		useCase.execute(new CachableRequest.Builder()
				.param1(65)
				.build());

		UseCase.unsubscribe(TestUseCase.class);

		useCase = UseCase.fetch(TestUseCase.class);
		useCase.subscribe(createCachedResultListener());
		useCase.execute(new CachableRequest.Builder()
				.param1(65)
				.build(),
				UseCase.CACHED);

		assertNotEquals(originalResult, cachedResult);
	}

	@Test
	public void executeDifferentRequests_returnNewData() throws Exception {

		CachableTestUseCase useCase;

		useCase = UseCase.fetch(CachableTestUseCase.class);
		useCase.subscribe(createOriginalResultListener());
		useCase.execute(new CachableRequest.Builder()
				.param1(65)
				.build());

		UseCase.unsubscribe(CachableTestUseCase.class);

		useCase = UseCase.fetch(CachableTestUseCase.class);
		useCase.subscribe(createCachedResultListener());
		useCase.execute(new CachableRequest.Builder()
				.param1(77)
				.build(),
				UseCase.CACHED);

		assertNotEquals(originalResult, cachedResult);
	}

	@Test
	public void executeSameRequest_returnCachedData() throws Exception {

		CachableTestUseCase useCase;

		useCase = UseCase.fetch(CachableTestUseCase.class);
		useCase.subscribe(createOriginalResultListener());
		useCase.execute(new CachableRequest.Builder()
				.param1(65)
				.build());

		useCase = UseCase.fetch(CachableTestUseCase.class);
		useCase.subscribe(createCachedResultListener());
		useCase.execute(new CachableRequest.Builder()
				.param1(65)
				.build(),
				UseCase.CACHED);

		assertEquals(originalResult, cachedResult);
	}

	@Test
	public void executeNewRequest_returnNewData() throws Exception {

		CachableTestUseCase useCase;

		useCase = UseCase.fetch(CachableTestUseCase.class);
		useCase.subscribe(createOriginalResultListener());
		useCase.execute(new CachableRequest.Builder()
				.param1(65)
				.build());

		UseCase.unsubscribe(CachableTestUseCase.class);

		useCase = UseCase.fetch(CachableTestUseCase.class);
		useCase.subscribe(createCachedResultListener());
		useCase.execute(new CachableRequest.Builder()
				.param1(66)
				.build(),
				UseCase.CACHED);

		assertNotEquals(originalResult, cachedResult);
		assertNotNull(cachedResult);
	}

	@Test
	public void executeSequential_returnNewData() throws Exception {

		int [] params = new int [] {45, 56};
		final Request [] requests = new Request[params.length];

		for (int i = 0; i < params.length; i++) {
			final int index = i;
			UseCase.fetch(CachableTestUseCase.class)
					.subscribe(new SimpleDisposableUseCaseListener<TestResult>() {
						@Override
						public void onUpdate(TestResult result) {
							requests[index] = result.request;
						}
					})
					.execute(new CachableRequest.Builder()
							.param1(params[i])
							.build());

		}

		assertNotEquals(requests[0], requests[1]);
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

	private static class CachableRequest extends Request {

		private final int param1;

		public static class Builder extends Request.Builder<Builder> {

			private int param1 = 0;

			public Builder param1(int val) {
				param1 = val;
				return this;
			}

			public CachableRequest build() { return new CachableRequest(this); }
		}

		protected CachableRequest(Builder builder) {
			super(builder);
			param1 = builder.param1;
		}

		@Override
		protected int id() {
			return param1;
		}
	}
}
package com.morkim.tectonic;

import android.support.annotation.NonNull;

import com.morkim.tectonic.entities.TestResult;
import com.morkim.tectonic.entities.TestUseCase;

import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;

public class CacheTest {

	private Result originalResult;
	private Result cachedResult;

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
		useCase.execute();

		useCase = new CachableTestUseCase();
		useCase.subscribe(createCachedResultListener());
		useCase.execute();

		assertNotEquals(originalResult, cachedResult);
	}

	@Test
	public void executeNonCachable_returnNewData() throws Exception {

		TestUseCase useCase;

		useCase = new TestUseCase();
		useCase.subscribe(createOriginalResultListener());
		useCase.execute();

		useCase = new TestUseCase();
		useCase.subscribe(createCachedResultListener());
		useCase.executeCached();

		assertNotEquals(originalResult, cachedResult);
	}

	@Test
	public void executeSecondTime_returnCachedData() throws Exception {

		TestUseCase useCase;

		useCase = new CachableTestUseCase();
		useCase.subscribe(createOriginalResultListener());
		useCase.execute();

		useCase = new CachableTestUseCase();
		useCase.subscribe(createCachedResultListener());
		useCase.executeCached();

		assertEquals(originalResult, cachedResult);
	}

	@Test
	public void executeAfterClear_returnNewData() throws Exception {

		TestUseCase useCase;

		useCase = new CachableTestUseCase();
		useCase.subscribe(createOriginalResultListener());
		useCase.execute();

		UseCase.clearCache(CachableTestUseCase.class);

		useCase = new CachableTestUseCase();
		useCase.subscribe(createCachedResultListener());
		useCase.executeCached();

		assertNotEquals(originalResult, cachedResult);
	}

	@NonNull
	private UseCase.OnUpdateListener<TestResult> createOriginalResultListener() {
		return new UseCase.OnUpdateListener<TestResult>() {
			@Override
			public void onUpdate(TestResult result) {
				originalResult = result;
			}
		};
	}

	@NonNull
	private UseCase.OnUpdateListener<TestResult> createCachedResultListener() {
		return new UseCase.OnUpdateListener<TestResult>() {
			@Override
			public void onUpdate(TestResult result) {
				cachedResult = result;
			}
		};
	}

	private class CachableTestUseCase extends TestUseCase {

		@Override
		protected boolean willCache() {
			return true;
		}
	}
}
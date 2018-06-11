package com.morkim.tectonic.simplified;

import com.morkim.tectonic.entities.CacheableData;
import com.morkim.tectonic.simplified.entities.CacheData;
import com.morkim.tectonic.simplified.entities.CachingUseCase;
import com.morkim.tectonic.simplified.entities.FinishedUseCase;
import com.morkim.tectonic.simplified.entities.NonCachingUseCase;
import com.morkim.tectonic.simplified.entities.OtherCacheData;
import com.morkim.tectonic.simplified.entities.WrongDataCachingUseCase;

import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotSame;
import static org.junit.Assert.assertTrue;

public class CacheTest {

	private int count;

	@BeforeClass
	public static void setupClass() {

	}

	@Before
	public void setup() {
		UseCase.clearAll();
		count = 0;
	}

	@Test
	public void no_caching__new_data_accessed() {

		final CacheData data1 = new CacheData();
		final CacheData data2 = new CacheData();

		NonCachingUseCase useCase = UseCase.fetch(NonCachingUseCase.class);
		NonCachingUseCase.Actor actor = new NonCachingUseCase.Actor() {

			@Override
			public CacheData requestData() {
				count++;
				return count == 1 ? data1 : data2;
			}
		};
		useCase.setActor(actor);
		useCase.execute();

		useCase.restart();

		assertEquals(1, data1.getAccessCount());
		assertEquals(1, data2.getAccessCount());
		assertEquals(2, count);
	}

	@Test
	public void cache_data__cached_data_accessed() {

		final CacheData data1 = new CacheData();
		final CacheData data2 = new CacheData();

		CachingUseCase useCase = UseCase.fetch(CachingUseCase.class);
		CachingUseCase.Actor actor = new CachingUseCase.Actor() {

			@Override
			public CacheData requestData() {
				count++;
				return count == 1 ? data1 : data2;
			}
		};
		useCase.setActor(actor);
		useCase.execute();

		useCase.restart();

		assertEquals(2, data1.getAccessCount());
		assertEquals(0, data2.getAccessCount());
		assertEquals(1, count);
	}

	@Test
	public void access_cached_data_with_wrong_key__class_cast_exception_thrown() {

		final CacheData data = new CacheData();
		final OtherCacheData otherData = new OtherCacheData();

		boolean classCastException = false;

		WrongDataCachingUseCase useCase = UseCase.fetch(WrongDataCachingUseCase.class);
		WrongDataCachingUseCase.Actor actor = new WrongDataCachingUseCase.Actor() {

			@Override
			public CacheData requestData() {
				return data;
			}

			@Override
			public OtherCacheData requestOtherData() {
				return otherData;
			}
		};
		useCase.setActor(actor);
		useCase.execute();

		try {
			useCase.restart();
		} catch (ClassCastException e) {
			classCastException = true;
		}

		assertTrue(classCastException);
	}
}
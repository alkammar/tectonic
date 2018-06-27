package com.morkim.tectonic.usecase;

import com.morkim.tectonic.usecase.entities.StepData;
import com.morkim.tectonic.usecase.entities.CachingUseCase;
import com.morkim.tectonic.usecase.entities.NonCachingUseCase;
import com.morkim.tectonic.usecase.entities.OtherStepData;
import com.morkim.tectonic.usecase.entities.WrongDataCachingUseCase;

import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class CacheTest extends TectonicTest {

	private int count;

	@Before
	public void setup() {
		super.setup();

		count = 0;
	}

	@Test
	public void no_caching__new_data_accessed() {

		final StepData data1 = new StepData();
		final StepData data2 = new StepData();

		NonCachingUseCase useCase = UseCase.fetch(NonCachingUseCase.class);
		NonCachingUseCase.Actor actor = new NonCachingUseCase.Actor() {

			@Override
			public StepData requestData() {
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

		final StepData data1 = new StepData();
		final StepData data2 = new StepData();

		CachingUseCase useCase = UseCase.fetch(CachingUseCase.class);
		CachingUseCase.Actor actor = new CachingUseCase.Actor() {

			@Override
			public StepData requestData() {
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

		final StepData data = new StepData();
		final OtherStepData otherData = new OtherStepData();

		boolean classCastException = false;

		WrongDataCachingUseCase useCase = UseCase.fetch(WrongDataCachingUseCase.class);
		WrongDataCachingUseCase.Actor actor = new WrongDataCachingUseCase.Actor() {

			@Override
			public StepData requestData() {
				return data;
			}

			@Override
			public OtherStepData requestOtherData() {
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
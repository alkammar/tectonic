package com.morkim.tectonic.usecase;

import com.morkim.tectonic.usecase.entities.NonCachingUseCase;
import com.morkim.tectonic.usecase.entities.StepData;

import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

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

		useCase.retry();

		assertEquals(1, data1.getAccessCount());
		assertEquals(1, data2.getAccessCount());
		assertEquals(2, count);
	}
}
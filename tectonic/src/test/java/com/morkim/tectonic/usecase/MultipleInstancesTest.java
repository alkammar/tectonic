package com.morkim.tectonic.usecase;

import com.morkim.tectonic.usecase.entities.InterruptableUseCase;
import com.morkim.tectonic.usecase.entities.SimpleUseCase;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertTrue;

public class MultipleInstancesTest extends ConcurrentTectonicTest {

	@Test
	public void same_instance_id__only_one_executed() {

		InterruptableUseCase useCase1 = UseCase.fetch(InterruptableUseCase.class, "1");
		InterruptableUseCase useCase2 = UseCase.fetch(InterruptableUseCase.class, "1");
		useCase1.execute();
		useCase2.execute();

		sleep();

		assertEquals(useCase1, useCase2);
		assertEquals(1, useCase1.getOnExecuteCalledCount());
		assertEquals(1, useCase2.getOnExecuteCalledCount());
	}

	@Test
	public void different_instance_id__only_one_executed() {

		InterruptableUseCase useCase1 = UseCase.fetch(InterruptableUseCase.class, "1");
		InterruptableUseCase useCase2 = UseCase.fetch(InterruptableUseCase.class, "2");
		useCase1.execute();
		useCase2.execute();

		sleep();

		assertNotEquals(useCase1, useCase2);
		assertEquals(1, useCase1.getOnExecuteCalledCount());
		assertEquals(1, useCase1.getOnExecuteCalledCount());
	}
}
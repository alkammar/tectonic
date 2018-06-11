package com.morkim.tectonic.simplified;

import com.morkim.tectonic.simplified.entities.FinishedUseCase;
import com.morkim.tectonic.simplified.entities.SimpleUseCase;

import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotSame;
import static org.junit.Assert.assertTrue;

public class FinishTest {

	@BeforeClass
	public static void setupClass() {

	}

	@Before
	public void setup() {
		UseCase.clearAll();
	}

	@Test
	public void start_finished_use_case__new_use_instance() {

		FinishedUseCase useCase1 = UseCase.fetch(FinishedUseCase.class);
		useCase1.execute();

		FinishedUseCase useCase2 = UseCase.fetch(FinishedUseCase.class);

		assertNotSame(useCase1, useCase2);
	}
}
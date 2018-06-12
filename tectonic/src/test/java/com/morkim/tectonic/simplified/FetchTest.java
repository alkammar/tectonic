package com.morkim.tectonic.simplified;

import com.morkim.tectonic.simplified.entities.CrashingUseCase;
import com.morkim.tectonic.simplified.entities.SimpleUseCase;

import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class FetchTest extends TectonicTest {

	@Test
	public void construct_new_use_case__callbacks_not_called() {

		SimpleUseCase useCase = new SimpleUseCase();
		useCase.execute();

		assertFalse(useCase.isOnCheckPreconditionsCalled());
		assertFalse(useCase.isOnExecuteCalled());
	}

	@Test
	public void fetch_crashing_use_case__error_exception() {

		boolean useCaseExists = true;
		try {
			CrashingUseCase useCase = UseCase.fetch(CrashingUseCase.class);
		} catch (UnableToInstantiateUseCase e) {
			useCaseExists = false;
		}

		assertFalse(useCaseExists);
	}

	@Test
	public void fetch_new_use_case__callbacks_called() {

		SimpleUseCase useCase = UseCase.fetch(SimpleUseCase.class);
		useCase.execute();

		assertTrue(useCase.isOnCheckPreconditionsCalled());
		assertTrue(useCase.isOnExecuteCalled());
	}

	@Test
	public void start_running_use_case__callbacks_not_called_again() {

		SimpleUseCase useCase = UseCase.fetch(SimpleUseCase.class);
		useCase.execute();

        UseCase.fetch(SimpleUseCase.class).execute();

		assertTrue(useCase.isOnCheckPreconditionsCalled());
		assertEquals(1, useCase.getOnExecuteCalledCount());
	}
}
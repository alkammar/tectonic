package com.morkim.tectonic.simplified;

import com.morkim.tectonic.simplified.entities.CompletedPreconditionsUseCase;
import com.morkim.tectonic.simplified.entities.FailingPreconditionsUseCase;
import com.morkim.tectonic.simplified.entities.SimpleUseCase;

import org.junit.Test;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class PreconditionsTest extends TectonicTest {

	@Test
	public void no_preconditions__onExecute_called() {

		SimpleUseCase useCase = UseCase.fetch(SimpleUseCase.class);
		useCase.execute();

		assertTrue(useCase.isOnExecuteCalled());
	}

	@Test
	public void preconditions_fail__onExecute_not_called() {

		FailingPreconditionsUseCase useCase = UseCase.fetch(FailingPreconditionsUseCase.class);
		useCase.execute();

		assertFalse(useCase.isOnExecuteCalled());
	}

	@Test
	public void preconditions_complete__onExecute_called() {

		CompletedPreconditionsUseCase useCase = UseCase.fetch(CompletedPreconditionsUseCase.class);
		useCase.execute();

		assertTrue(useCase.isOnExecuteCalled());
	}
}
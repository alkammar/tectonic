package com.morkim.tectonic.simplified;

import com.morkim.tectonic.simplified.entities.CompletedPreconditionsUseCase;
import com.morkim.tectonic.simplified.entities.CompletedUseCase;
import com.morkim.tectonic.simplified.entities.FailingPreconditionsUseCase;
import com.morkim.tectonic.simplified.entities.SimpleUseCase;

import org.junit.Test;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class PreconditionsTest extends ConcurrentTectonicTest {

	@Test
	public void no_preconditions__onExecute_called() throws InterruptedException {

		CompletedUseCase useCase = UseCase.fetch(CompletedUseCase.class);
		useCase.execute();

		useCaseThread.join();

		assertTrue(useCase.isOnExecuteCalled());
	}

	@Test
	public void preconditions_fail__onExecute_not_called() {

		FailingPreconditionsUseCase useCase = UseCase.fetch(FailingPreconditionsUseCase.class);
		useCase.execute();

		assertFalse(useCase.isOnExecuteCalled());
	}

	@Test
	public void preconditions_complete__onExecute_called() throws InterruptedException {

		final CompletedPreconditionsUseCase useCase = UseCase.fetch(CompletedPreconditionsUseCase.class);
		useCase.execute();

		Thread thread = new Thread(new Runnable() {
			@Override
			public void run() {
				sleep();
				useCase.onComplete(CompletedPreconditionsUseCase.SUCCESSFUL_EVENT);
			}
		});
		thread.start();

		sleep();
		sleep();
		sleep();
		sleep();

		assertTrue(useCase.isOnExecuteCalled());
	}
}
package com.morkim.tectonic.usecase;

import com.morkim.tectonic.usecase.entities.CompletedUseCase;

import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertNotSame;
import static org.junit.Assert.assertTrue;

public class CompleteTest extends TectonicTest {

	private boolean stopCalled;

	@Before
	public void setup() {
		super.setup();

		stopCalled = false;
	}

	@Test
	public void start_completed_use_case__new_use_instance() {

		UseCase.defaultThreadManager(new ThreadManager() {
			@Override
			public boolean isRunning() {
				return true;
			}

			@Override
			public void release() throws InterruptedException {

			}

			@Override
			public void start(UseCaseExecution execution) {
				try {
					execution.run();
				} catch (InterruptedException e) {
					e.printStackTrace();
				} catch (UndoException e) {
					e.printStackTrace();
				}
			}

			@Override
			public void stop() {
				stopCalled = true;
			}

			@Override
			public void restart() {

			}
		});

		CompletedUseCase useCase1 = UseCase.fetch(CompletedUseCase.class);
		useCase1.execute();

		assertTrue(stopCalled);

		CompletedUseCase useCase2 = UseCase.fetch(CompletedUseCase.class);

		assertNotSame(useCase1, useCase2);
	}
}
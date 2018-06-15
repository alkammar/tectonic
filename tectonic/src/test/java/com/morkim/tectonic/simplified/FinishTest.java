package com.morkim.tectonic.simplified;

import com.morkim.tectonic.simplified.entities.FinishedUseCase;

import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertNotSame;
import static org.junit.Assert.assertTrue;

public class FinishTest extends TectonicTest{

	private boolean stopCalled;

	@Before
	public void setup() {
		super.setup();

		stopCalled = false;
	}

	@Test
	public void start_finished_use_case__new_use_instance() {

		UseCase.defaultThreadManager(new ThreadManager() {
			@Override
			public boolean isRunning() {
				return true;
			}

			@Override
			public void start(UseCaseExecution execution) {
				try {
					execution.run();
				} catch (InterruptedException e) {
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

		FinishedUseCase useCase1 = UseCase.fetch(FinishedUseCase.class);
		useCase1.execute();

		assertTrue(stopCalled);

		FinishedUseCase useCase2 = UseCase.fetch(FinishedUseCase.class);

		assertNotSame(useCase1, useCase2);
	}
}
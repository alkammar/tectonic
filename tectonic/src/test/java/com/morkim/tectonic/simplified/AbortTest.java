package com.morkim.tectonic.simplified;

import com.morkim.tectonic.flow.Step;
import com.morkim.tectonic.simplified.entities.FinishedUseCase;
import com.morkim.tectonic.simplified.entities.SimpleUseCase;

import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class AbortTest extends TectonicTest {

	private boolean onAbortCalled;
	private UseCaseHandle handle;

	@Before
	public void setup() {
		super.setup();

		onAbortCalled = false;
	}

	@Test
	public void abort_not_started__onAbort_not_called() {

		SimpleUseCase useCase = UseCase.fetch(SimpleUseCase.class);
		useCase.setPrimaryActor(new SimpleUseCase.Actor() {

			@Override
			public void onStart(UseCaseHandle handle) {

			}

			@Override
			public void onUndo(Step step) {

			}

			@Override
			public void onAbort() {
				onAbortCalled = true;
			}
		});

		assertFalse(onAbortCalled);
	}

	@Test
	public void abort_running_use_case__onAbort_called() {

		SimpleUseCase useCase = UseCase.fetch(SimpleUseCase.class);
		useCase.setPrimaryActor(new SimpleUseCase.Actor() {

			@Override
			public void onStart(UseCaseHandle handle) {
				AbortTest.this.handle = handle;
				handle.abort();
			}

			@Override
			public void onUndo(Step step) {

			}

			@Override
			public void onAbort() {
				onAbortCalled = true;
			}
		});
		useCase.execute();

		assertTrue(onAbortCalled);
	}

	@Test
	public void abort_finished_use_case__onAbort_not_called() {

		FinishedUseCase useCase = UseCase.fetch(FinishedUseCase.class);
		useCase.setPrimaryActor(new FinishedUseCase.Actor() {

			@Override
			public void onStart(UseCaseHandle handle) {
				AbortTest.this.handle = handle;
			}

			@Override
			public void onUndo(Step step) {

			}

			@Override
			public void onAbort() {
				onAbortCalled = true;
			}
		});
		useCase.execute();
		handle.abort();

		assertFalse(onAbortCalled);
	}
}
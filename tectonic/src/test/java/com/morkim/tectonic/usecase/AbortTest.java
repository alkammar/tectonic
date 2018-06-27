package com.morkim.tectonic.usecase;

import com.morkim.tectonic.flow.Step;
import com.morkim.tectonic.usecase.entities.CompletedUseCase;
import com.morkim.tectonic.usecase.entities.SimpleUseCase;

import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class AbortTest extends TectonicTest {

	private boolean onAbortCalled;
	private UseCaseHandle handle;
	private boolean onCompleteCalled;

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
			public void onComplete(Integer event, Void result) {
				onCompleteCalled = true;
			}

			@Override
			public void onUndo(Step step) {

			}

			@Override
			public void onAbort(Integer event) {
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
			public void onComplete(Integer event, Void result) {
				onCompleteCalled = true;
			}

			@Override
			public void onUndo(Step step) {

			}

			@Override
			public void onAbort(Integer event) {
				onAbortCalled = true;
			}
		});
		useCase.execute();

		assertTrue(onAbortCalled);
		assertFalse(onCompleteCalled);
	}

	@Test
	public void abort_completed_use_case__onAbort_not_called() {

		CompletedUseCase useCase = UseCase.fetch(CompletedUseCase.class);
		useCase.setPrimaryActor(new CompletedUseCase.Actor() {

			@Override
			public void onStart(UseCaseHandle handle) {
				AbortTest.this.handle = handle;
			}

			@Override
			public void onComplete(Integer event, Void result) {

			}

			@Override
			public void onComplete(Integer event) {
				onCompleteCalled = true;
			}

			@Override
			public void onUndo(Step step) {

			}

			@Override
			public void onAbort(Integer event) {
				onAbortCalled = true;
			}
		});
		useCase.execute();
		handle.abort();

		assertFalse(onAbortCalled);
	}
}
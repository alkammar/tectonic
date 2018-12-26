package com.morkim.tectonic.usecase;

import com.morkim.tectonic.flow.Step;
import com.morkim.tectonic.usecase.entities.CompletedUseCase;

import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class PrimaryActorTest extends TectonicTest {

	private boolean onStartCalled;
	private boolean onCompleteCalled;
	private boolean onAbortCalled;
	private PrimaryHandle handle;

	@Before
	public void setup() {
		super.setup();

		onStartCalled = false;
		onAbortCalled = false;
	}

	@Test
	public void no_primary_actor__callbacks_not_called() {

		CompletedUseCase useCase = UseCase.fetch(CompletedUseCase.class);
		useCase.setActor(new CompletedUseCase.Actor() {

			@Override
			public void onStart(TectonicEvent event, PrimaryHandle handle) {
				onStartCalled = true;
			}

			@Override
			public void onComplete(TectonicEvent event, Void result) {

			}

			@Override
			public void onComplete(TectonicEvent event) {
				onCompleteCalled = true;
			}

			@Override
			public void onUndo(Step step) {

			}

			@Override
			public void onAbort(TectonicEvent event) {
				onAbortCalled = true;
			}
		});
		useCase.execute();

		assertFalse(onStartCalled);
		assertFalse(onAbortCalled);
	}

	@Test
	public void completed_use_case__callbacks_called() {

		CompletedUseCase useCase = UseCase.fetch(CompletedUseCase.class);
		CompletedUseCase.Actor actor = new CompletedUseCase.Actor() {

			@Override
			public void onStart(TectonicEvent event, PrimaryHandle handle) {
				PrimaryActorTest.this.handle = handle;
				onStartCalled = true;
			}

			@Override
			public void onComplete(TectonicEvent event, Void result) {
				onCompleteCalled = true;
			}

			@Override
			public void onComplete(TectonicEvent event) {

			}

			@Override
			public void onUndo(Step step) {

			}

			@Override
			public void onAbort(TectonicEvent event) {
				onAbortCalled = true;
			}
		};
		useCase.setPrimaryActor(actor);
		useCase.setActor(actor);
		useCase.execute();

		assertTrue(onStartCalled);
		assertTrue(onCompleteCalled);
	}
}
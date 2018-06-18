package com.morkim.tectonic.simplified;

import com.morkim.tectonic.flow.Step;
import com.morkim.tectonic.simplified.entities.FinishedUseCase;

import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotSame;
import static org.junit.Assert.assertTrue;

public class PrimaryActorTest extends TectonicTest {

	private boolean onStartCalled;
	private boolean onAbortCalled;
	private UseCaseHandle handle;

	@Before
	public void setup() {
		super.setup();

		onStartCalled = false;
		onAbortCalled = false;
	}

	@Test
	public void no_primary_actor__callbacks_not_called() {

		FinishedUseCase useCase = UseCase.fetch(FinishedUseCase.class);
		useCase.setActor(new FinishedUseCase.Actor() {

			@Override
			public void onStart(UseCaseHandle handle) {
				onStartCalled = true;
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

		assertFalse(onStartCalled);
		assertFalse(onAbortCalled);
	}

	@Test
	public void primary_actor__callbacks_called() {

		FinishedUseCase useCase = UseCase.fetch(FinishedUseCase.class);
		FinishedUseCase.Actor actor = new FinishedUseCase.Actor() {

			@Override
			public void onStart(UseCaseHandle handle) {
				PrimaryActorTest.this.handle = handle;
				onStartCalled = true;
				handle.abort();
			}

			@Override
			public void onUndo(Step step) {

			}

			@Override
			public void onAbort() {
				onAbortCalled = true;
			}
		};
		useCase.setPrimaryActor(actor);
		useCase.setActor(actor);
		useCase.execute();

		assertTrue(onStartCalled);
		assertTrue(onAbortCalled);
	}
}
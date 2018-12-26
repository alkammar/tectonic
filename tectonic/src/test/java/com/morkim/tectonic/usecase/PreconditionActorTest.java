package com.morkim.tectonic.usecase;

import com.morkim.tectonic.flow.Step;
import com.morkim.tectonic.usecase.entities.CompletedUseCase;

import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class PreconditionActorTest extends TectonicTest {

	private boolean onStartCalled;
	private boolean onCompleteCalled;
	private boolean onAbortCalled;
	private int onCompleteCalledCount;

	@Before
	public void setup() {
		super.setup();

		onStartCalled = false;
		onCompleteCalled = false;
		onAbortCalled = false;

		onCompleteCalledCount = 0;
	}

	@Test
	public void no_precondition_actor__callbacks_not_called() {

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
				onStartCalled = true;
			}

			@Override
			public void onComplete(TectonicEvent event, Void result) {

			}

			@Override
			public void onComplete(TectonicEvent event) {
				onCompleteCalled = true;
				onCompleteCalledCount++;
			}

			@Override
			public void onUndo(Step step) {

			}

			@Override
			public void onAbort(TectonicEvent event) {
				onAbortCalled = true;
			}
		};
		useCase.setPreconditionActor(actor);
		useCase.setActor(actor);
		useCase.execute();

		assertTrue(onCompleteCalled);
		assertEquals(1, onCompleteCalledCount);
	}
}
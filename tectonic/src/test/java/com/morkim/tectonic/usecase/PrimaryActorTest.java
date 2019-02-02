package com.morkim.tectonic.usecase;

import com.morkim.tectonic.flow.Step;
import com.morkim.tectonic.usecase.entities.CompletedUseCase;

import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class PrimaryActorTest extends TectonicTest {

	private boolean onStartCalled;
	private boolean onCompleteCalled;
	private boolean onAbortCalled;
	private UseCaseHandle handle;

	private int onStartCount;
	private int onCompleteCount;

	@Before
	public void setup() {
		super.setup();

		onStartCalled = false;
		onAbortCalled = false;

		onStartCount = 0;
		onCompleteCount = 0;
	}

	@Test
	public void no_primary_actor__callbacks_not_called() {

		CompletedUseCase useCase = UseCase.fetch(CompletedUseCase.class);
		useCase.setUnknownActor(new Actor<TectonicEvent>() {

			@Override
			public void onStart(TectonicEvent event, UseCaseHandle handle) {
				onStartCalled = true;
			}

			@Override
			public void onComplete(TectonicEvent event) {
				onCompleteCalled = true;
			}

			@Override
			public void onUndo(Step step, boolean inclusive) {

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
			public void doSomething() {

			}

			@Override
			public void onStart(TectonicEvent event, UseCaseHandle handle) {
				PrimaryActorTest.this.handle = handle;
				onStartCalled = true;
			}

			@Override
			public void onComplete(TectonicEvent event) {
				onCompleteCalled = true;
			}

			@Override
			public void onUndo(Step step, boolean inclusive) {

			}

			@Override
			public void onAbort(TectonicEvent event) {
				onAbortCalled = true;
			}
		};
		useCase.addPrimaryActor(actor);
		useCase.execute();

		assertTrue(onStartCalled);
		assertTrue(onCompleteCalled);
	}

	@Test
	public void completed_use_case_with_multiple_primary_actors__callbacks_called() {

		onStartCount = 0;
		onCompleteCount = 0;

		CompletedUseCase useCase = UseCase.fetch(CompletedUseCase.class);
		useCase.addPrimaryActor(new CompletedUseCase.Actor() {

			@Override
			public void doSomething() {

			}

			@Override
            public void onStart(TectonicEvent event, UseCaseHandle handle) {
                PrimaryActorTest.this.handle = handle;
                onStartCount++;
            }

            @Override
            public void onComplete(TectonicEvent event) {
                onCompleteCount++;
            }

            @Override
            public void onUndo(Step step, boolean inclusive) {

            }

            @Override
            public void onAbort(TectonicEvent event) {
                onAbortCalled = true;
            }
        });
		useCase.addPrimaryActor(new CompletedUseCase.Actor() {

			@Override
			public void doSomething() {

			}

			@Override
            public void onStart(TectonicEvent event, UseCaseHandle handle) {
                PrimaryActorTest.this.handle = handle;
                onStartCount++;
            }

            @Override
            public void onComplete(TectonicEvent event) {
                onCompleteCount++;
            }

            @Override
            public void onUndo(Step step, boolean inclusive) {

            }

            @Override
            public void onAbort(TectonicEvent event) {
                onAbortCalled = true;
            }
        });
		useCase.execute();

		assertEquals(2, onStartCount);
		assertEquals(2, onCompleteCount);
	}
}
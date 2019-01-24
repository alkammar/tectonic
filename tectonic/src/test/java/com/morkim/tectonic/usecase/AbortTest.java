package com.morkim.tectonic.usecase;

import com.morkim.tectonic.flow.Step;
import com.morkim.tectonic.usecase.entities.CompletedUseCase;
import com.morkim.tectonic.usecase.entities.FailingPreconditionsUseCase;
import com.morkim.tectonic.usecase.entities.InterruptableUseCase;
import com.morkim.tectonic.usecase.entities.SimpleTriggers;
import com.morkim.tectonic.usecase.entities.SimpleUseCase;

import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class AbortTest extends ConcurrentTectonicTest {

	private volatile boolean onAbortCalled;
	private UseCaseHandle handle;
	private boolean onCompleteCalled;
	private int onAbortCalledCount;

	@Before
	public void setup() {
		super.setup();

		onAbortCalled = false;
		onAbortCalledCount = 0;
	}

	@Test
	public void abort_not_started__onAbort_not_called() {

		SimpleUseCase useCase = UseCase.fetch(SimpleUseCase.class);
		useCase.addPrimaryActor(new SimpleUseCase.SimpleActor() {

			@Override
			public void onStart(Integer event, UseCaseHandle handle) {

			}

			@Override
			public void onComplete(Integer event) {
				onCompleteCalled = true;
			}

			@Override
			public void onUndo(Step step, boolean inclusive) {

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

		InterruptableUseCase useCase = UseCase.fetch(InterruptableUseCase.class);
		useCase.addPrimaryActor(new SimpleUseCase.SimpleActor() {

			@Override
			public void onStart(Integer event, UseCaseHandle handle) {
				AbortTest.this.handle = handle;
			}

			@Override
			public void onComplete(Integer event) {
				onCompleteCalled = true;
			}

			@Override
			public void onUndo(Step step, boolean inclusive) {

			}

			@Override
			public void onAbort(Integer event) {
				onAbortCalledCount++;
			}
		});
		useCase.setPreconditionActor(new PreconditionActor<TectonicEvent>() {
			@Override
			public void onComplete(TectonicEvent event) {

			}

			@Override
			public void onAbort(TectonicEvent event) {
				onAbortCalledCount++;
			}
		});
		useCase.addResultActor(new ResultActor<TectonicEvent, Void>() {

			@Override
			public void onComplete(TectonicEvent event, Void result) {

			}

			@Override
			public void onAbort(TectonicEvent event) {
				onAbortCalledCount++;
			}
		});
		useCase.execute();

		sleep();

		useCase.abort();

		try {
			useCaseThread.join();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}

		assertEquals(3, onAbortCalledCount);
		assertFalse(onCompleteCalled);
	}

	@Test
	public void abort_while_waiting_for_preconditions__onAbort_called() {


		FailingPreconditionsUseCase useCase = UseCase.fetch(FailingPreconditionsUseCase.class);
		useCase.setExecutor(new SimpleTriggers());
		useCase.setPreconditionActor(new PreconditionActor<TectonicEvent>() {
			@Override
			public void onComplete(TectonicEvent event) {

			}

			@Override
			public void onAbort(TectonicEvent event) {
				onAbortCalledCount++;
			}
		});
		useCase.execute();

		sleep();

		useCase.abort();

		try {
			useCaseThread.join();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}

		assertEquals(1, onAbortCalledCount);
		assertFalse(onCompleteCalled);
	}

	@Test
	public void abort_completed_use_case__onAbort_not_called() {

		CompletedUseCase useCase = UseCase.fetch(CompletedUseCase.class);
		useCase.addPrimaryActor(new CompletedUseCase.Actor() {

			@Override
			public void onStart(TectonicEvent event, UseCaseHandle handle) {
				AbortTest.this.handle = handle;
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
				onAbortCalledCount++;
			}
		});
		useCase.setPreconditionActor(new PreconditionActor<TectonicEvent>() {
			@Override
			public void onComplete(TectonicEvent event) {

			}

			@Override
			public void onAbort(TectonicEvent event) {
				onAbortCalledCount++;
			}
		});
		useCase.addResultActor(new ResultActor<TectonicEvent, Void>() {

			@Override
			public void onComplete(TectonicEvent event, Void result) {

			}

			@Override
			public void onAbort(TectonicEvent event) {
				onAbortCalledCount++;
			}
		});
		useCase.execute();

		try {
			useCaseThread.join();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}

		useCase.abort();

		assertEquals(0, onAbortCalledCount);
	}

	@Test
	public void abort_from_secondary_actor__onAbort_not_called() {

		InterruptableUseCase useCase = UseCase.fetch(InterruptableUseCase.class);
		useCase.addSecondaryActor(new SecondaryActor<Integer>() {

			@Override
			public void onStart(Integer event, UseCaseHandle handle) {
				AbortTest.this.handle = handle;
			}

			@Override
			public void onComplete(Integer event) {
				onCompleteCalled = true;
			}

			@Override
			public void onUndo(Step step, boolean inclusive) {

			}

			@Override
			public void onAbort(Integer event) {
				onAbortCalledCount++;
			}
		});
		useCase.setPreconditionActor(new PreconditionActor<TectonicEvent>() {
			@Override
			public void onComplete(TectonicEvent event) {

			}

			@Override
			public void onAbort(TectonicEvent event) {
				onAbortCalledCount++;
			}
		});
		useCase.addResultActor(new ResultActor<TectonicEvent, Void>() {

			@Override
			public void onComplete(TectonicEvent event, Void result) {

			}

			@Override
			public void onAbort(TectonicEvent event) {
				onAbortCalledCount++;
			}
		});
		useCase.execute();

		sleep();

		handle.abort();

		try {
			useCaseThread.join();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}

		assertEquals(0, onAbortCalledCount);
		assertTrue(onCompleteCalled);
	}
}
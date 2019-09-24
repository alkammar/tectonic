package com.morkim.tectonic.usecase;

import com.morkim.tectonic.flow.Step;
import com.morkim.tectonic.usecase.entities.AbortedUseCase;
import com.morkim.tectonic.usecase.entities.CompletedPreconditionsUseCase;
import com.morkim.tectonic.usecase.entities.CompletedUseCase;
import com.morkim.tectonic.usecase.entities.FailingPreconditionsUseCase;
import com.morkim.tectonic.usecase.entities.SimpleTriggers;
import com.morkim.tectonic.usecase.entities.SimpleUseCase;

import org.junit.Test;

import java.util.concurrent.ExecutionException;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class PreconditionsTest extends ConcurrentTectonicTest {

	private boolean onAbortCalled;
	private volatile boolean onSubObserverCompleteCalled;
	private volatile boolean onSubObserverAbortedCalled;

	private PreconditionCompletedActor preconditionCompletedActor = new PreconditionCompletedActor();

	@Override
	public void setup() {
		super.setup();

		onSubObserverCompleteCalled = false;
		onSubObserverAbortedCalled = false;
	}

	@Test
	public void no_preconditions__onExecute_called() throws InterruptedException {

		CompletedUseCase useCase = UseCase.fetch(CompletedUseCase.class);
		useCase.execute();

		waitForUseCaseToFinish();

		assertTrue(useCase.isOnExecuteCalled());
	}

	@Test
	public void preconditions_fail__onExecute_not_called() throws InterruptedException {

		FailingPreconditionsUseCase useCase = UseCase.fetch(FailingPreconditionsUseCase.class);
		useCase.setExecutor(new TestExecutor());
		AbortedUseCase sub = UseCase.fetch(AbortedUseCase.class);
		sub.addPrimaryActor(preconditionCompletedActor);
		ThreadManagerImpl subThreadManager = new ThreadManagerImpl();
		sub.setThreadManager(subThreadManager);
		useCase.addPrimaryActor(new SimpleUseCase.SimpleActor() {
			@Override
			public void onStart(Integer event, UseCaseHandle handle) {

			}

			@Override
			public void onUndo(Step step, boolean inclusive) {

			}

			@Override
			public void onComplete(Integer event) {

			}

			@Override
			public void onAbort(Integer event) {
				onAbortCalled = true;
			}
		});
		useCase.execute();
//
//		UseCase.fetch(SimpleUseCase.class).abort();

		waitForUseCaseToFinish();

		assertFalse(useCase.isOnExecuteCalled());
		assertTrue(onAbortCalled);
	}

	@Test
	public void preconditions_complete__onExecute_called() throws InterruptedException {

		final CompletedPreconditionsUseCase useCase = UseCase.fetch(CompletedPreconditionsUseCase.class);
		useCase.setExecutor(new TestExecutor());
		CompletedUseCase sub = UseCase.fetch(CompletedUseCase.class);
		sub.addPrimaryActor(preconditionCompletedActor);
		ThreadManagerImpl subThreadManager = new ThreadManagerImpl();
		sub.setThreadManager(subThreadManager);
		useCase.execute();

		sleep();
		sleep();
		sleep();
		sleep();

//		subThreadManager.thread.join();

		waitForUseCaseToFinish();

		assertTrue(useCase.isOnExecuteCalled());
	}

	private class TestExecutor extends SimpleTriggers {

		@Override
		public ResultActor<TectonicEvent, ?> observe(TectonicEvent contextEvent, TectonicEvent subEvent, UseCase<?> useCase) {
			return new ResultActor<TectonicEvent, Object>() {
				@Override
				public void onComplete(TectonicEvent event, Object result) {
					onSubObserverCompleteCalled = true;
				}

				@Override
				public void onAbort(TectonicEvent event) {
					onSubObserverAbortedCalled = true;
				}
			};
		}
	}

	private class PreconditionCompletedActor implements CompletedUseCase.Actor {

		@Override
		public void onStart(TectonicEvent event, UseCaseHandle handle) {
		}

		@Override
		public void onUndo(Step step, boolean inclusive) {
		}

		@Override
		public void onComplete(TectonicEvent event) {
		}

		@Override
		public void onAbort(TectonicEvent event) {
		}

		@Override
		public void doSomething() throws InterruptedException, ExecutionException, UndoException {
			Thread.sleep(100);
		}
	}
}
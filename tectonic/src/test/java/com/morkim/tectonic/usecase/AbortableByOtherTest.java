package com.morkim.tectonic.usecase;

import com.morkim.tectonic.flow.Step;
import com.morkim.tectonic.usecase.entities.AbortableByOtherAbortionUseCase;
import com.morkim.tectonic.usecase.entities.AbortableByOtherCompletionUseCase;
import com.morkim.tectonic.usecase.entities.CompletedUseCase;
import com.morkim.tectonic.usecase.entities.SimpleUseCase;

import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertTrue;

public class AbortableByOtherTest {

	private boolean aborted;

	@Before
	public void setup() {
		UseCase.clearAll();
		UseCase.setGlobalThreadManager(null);
	}

	@Test
	public void complete_use_case__aborts_another_use_case() throws InterruptedException {

		AbortableByOtherCompletionUseCase abortableByOther = UseCase.fetch(AbortableByOtherCompletionUseCase.class);
		abortableByOther.addPrimaryActor(new SimpleUseCase.SimpleActor() {
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
				aborted = true;
			}
		});
		abortableByOther.execute();

		CompletedUseCase abortingOther = UseCase.fetch(CompletedUseCase.class);
		abortingOther.execute();

		try {
			Thread.sleep(100);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}

		assertTrue(aborted);
	}

	@Test
	public void abort_use_case__aborts_another_use_case() throws InterruptedException {

		AbortableByOtherAbortionUseCase abortableByOther = UseCase.fetch(AbortableByOtherAbortionUseCase.class);
		abortableByOther.addPrimaryActor(new SimpleUseCase.SimpleActor() {
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
				aborted = true;
			}
		});
		abortableByOther.execute();

		SimpleUseCase abortingOther = UseCase.fetch(SimpleUseCase.class);
		abortingOther.execute();

		try {
			Thread.sleep(100);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}

		abortingOther.abort();

		try {
			Thread.sleep(100);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}

		assertTrue(aborted);
	}
}
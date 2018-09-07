package com.morkim.tectonic.usecase;

import com.morkim.tectonic.flow.Step;
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
		UseCase.defaultThreadManager(null);
	}

	@Test
	public void complete_use_case__completes_another_use_case() throws InterruptedException {

		AbortableByOtherCompletionUseCase completableByOther = UseCase.fetch(AbortableByOtherCompletionUseCase.class);
		completableByOther.setPrimaryActor(new SimpleUseCase.Actor() {
			@Override
			public void onStart(Integer event, UseCaseHandle handle) {

			}

			@Override
			public void onUndo(Step step) {

			}

			@Override
			public void onComplete(Integer event, Void result) {

			}

			@Override
			public void onAbort(Integer event) {
				aborted = true;
			}
		});
		completableByOther.execute();

		CompletedUseCase completingOther = UseCase.fetch(CompletedUseCase.class);
		completingOther.execute();

		try {
			Thread.sleep(100);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}

		assertTrue(aborted);
	}
}
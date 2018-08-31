package com.morkim.tectonic.usecase;

import com.morkim.tectonic.flow.Step;
import com.morkim.tectonic.usecase.entities.CompletableByOtherCompletionUseCase;
import com.morkim.tectonic.usecase.entities.CompletingOtherUseCaseUseCase;
import com.morkim.tectonic.usecase.entities.SimpleUseCase;

import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertTrue;

public class CompletableByOtherTest {

	private boolean completed;

	@Before
	public void setup() {
		UseCase.clearAll();
		UseCase.defaultThreadManager(null);
	}

	@Test
	public void complete_use_case__completes_another_use_case() throws InterruptedException {

		CompletableByOtherCompletionUseCase completableByOther = UseCase.fetch(CompletableByOtherCompletionUseCase.class);
		completableByOther.setPrimaryActor(new SimpleUseCase.Actor() {
			@Override
			public void onStart(Integer event, UseCaseHandle handle) {

			}

			@Override
			public void onUndo(Step step) {

			}

			@Override
			public void onComplete(Integer event, Void result) {
				completed = true;
			}

			@Override
			public void onAbort(Integer event) {

			}
		});
		completableByOther.execute();

		CompletingOtherUseCaseUseCase completingOther = UseCase.fetch(CompletingOtherUseCaseUseCase.class);
		completingOther.execute();

		try {
			Thread.sleep(100);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}

		assertTrue(completed);
	}
}
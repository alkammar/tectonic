package com.morkim.tectonic.usecase;

import com.morkim.tectonic.flow.Step;
import com.morkim.tectonic.usecase.entities.CompletableByOtherAbortionUseCase;
import com.morkim.tectonic.usecase.entities.CompletableByOtherCompletionUseCase;
import com.morkim.tectonic.usecase.entities.CompletedUseCase;
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

		completed = false;
	}

	@Test
	public void complete_use_case__completes_another_use_case() throws InterruptedException {

		CompletableByOtherCompletionUseCase completableByOther = UseCase.fetch(CompletableByOtherCompletionUseCase.class);
		completableByOther.addPrimaryActor(new SimpleUseCase.SimpleActor() {
			@Override
			public void onStart(Integer event, UseCaseHandle handle) {

			}

			@Override
			public void onUndo(Step step, boolean inclusive) {

			}

			@Override
			public void onComplete(Integer event) {
				completed = true;
			}

			@Override
			public void onAbort(Integer event) {

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

		assertTrue(completed);
	}

	@Test
	public void abort_use_case__completes_another_use_case() throws InterruptedException {

		CompletableByOtherAbortionUseCase completableByOther = UseCase.fetch(CompletableByOtherAbortionUseCase.class);
		completableByOther.addPrimaryActor(new SimpleUseCase.SimpleActor() {
			@Override
			public void onStart(Integer event, UseCaseHandle handle) {

			}

			@Override
			public void onUndo(Step step, boolean inclusive) {

			}

			@Override
			public void onComplete(Integer event) {
				completed = true;
			}

			@Override
			public void onAbort(Integer event) {

			}
		});
		completableByOther.execute();

		SimpleUseCase interruptableUseCase = UseCase.fetch(SimpleUseCase.class);
		interruptableUseCase.execute();

		try {
			Thread.sleep(100);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}

		interruptableUseCase.abort();

		try {
			Thread.sleep(100);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}

		assertTrue(completed);
	}
}
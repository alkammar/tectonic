package com.morkim.tectonic;

import android.support.annotation.NonNull;

import com.morkim.tectonic.entities.PendingActionRequest;
import com.morkim.tectonic.entities.RequestInputTestUseCase;

import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import java.util.List;
import java.util.concurrent.Callable;

import io.reactivex.Scheduler;
import io.reactivex.android.plugins.RxAndroidPlugins;
import io.reactivex.functions.Function;
import io.reactivex.plugins.RxJavaPlugins;
import io.reactivex.schedulers.Schedulers;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class InputRequiredExecutionTest extends TecTonicTest {

	private int firstSubscriberNotified;
	private int secondSubscriberNotified;
	private int thirdSubscriberNotified;

	@BeforeClass
	public static void setupClass() {

		RxAndroidPlugins.setInitMainThreadSchedulerHandler(new Function<Callable<Scheduler>, Scheduler>() {
			@Override
			public Scheduler apply(@NonNull Callable<Scheduler> schedulerCallable) throws Exception {
				return Schedulers.trampoline();
			}
		});

		RxJavaPlugins.setIoSchedulerHandler(new Function<Scheduler, Scheduler>() {
			@Override
			public Scheduler apply(@io.reactivex.annotations.NonNull Scheduler scheduler) throws Exception {
				return Schedulers.trampoline();
			}
		});
	}

	@Before
	public void setup() {

		UseCase.unsubscribeAll();
		UseCase.clearAllInProgress();
	}

	@Test
	public void multipleSubscriptions_onlyTheLastSubscriberIsNotified() throws Exception {

		RequestInputTestUseCase
		useCase = UseCase.fetch(RequestInputTestUseCase.class);
		useCase.subscribe(new SimpleUseCaseListener<Result>() {
			@Override
			public boolean onInputRequired(List<Integer> codes) {
				firstSubscriberNotified++;
				return true;
			}
		});
		useCase.subscribe(new SimpleUseCaseListener<Result>() {
			@Override
			public boolean onInputRequired(List<Integer> codes) {
				secondSubscriberNotified++;
				return true;
			}
		});
		useCase.subscribe(new SimpleUseCaseListener<Result>() {
			@Override
			public boolean onInputRequired(List<Integer> codes) {
				thirdSubscriberNotified++;
				return true;
			}
		});
		useCase.execute();

		assertEquals(0, firstSubscriberNotified);
		assertEquals(0, secondSubscriberNotified);
		assertEquals(1, thirdSubscriberNotified);
	}

	@Test
	public void executeFirstTimeMissingInput_executionContinuesWhenInputsAreComplete() throws Exception {

		RequestInputTestUseCase

		useCase = UseCase.fetch(RequestInputTestUseCase.class);
		useCase.execute(new PendingActionRequest.Builder()
				.input1("asdfas")
				.input2("")
				.build());

		useCase = UseCase.fetch(RequestInputTestUseCase.class);
		useCase.execute(new PendingActionRequest.Builder()
				.input1("asdfas")
				.input2("qw98ewu9j")
				.build());

		assertTrue(useCase.isExecutionContinued());
	}

	@Test
	public void executeMultiple_onlyNewSubscriptionOnStartAndOnExecute() throws Exception {

		RequestInputTestUseCase
		useCase = UseCase.fetch(RequestInputTestUseCase.class);
		useCase.subscribe(new SimpleUseCaseListener<Result>() {
			@Override
			public boolean onInputRequired(List<Integer> codes) {
				return true;
			}
		});
		useCase.execute();

		useCase = UseCase.fetch(RequestInputTestUseCase.class);
		useCase.execute(new PendingActionRequest.Builder()
				.input1("asdfas")
				.input2("qw98ewu9j")
				.build());

		assertTrue(useCase.isExecutionContinued());
	}
}
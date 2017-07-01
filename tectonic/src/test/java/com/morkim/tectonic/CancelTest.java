package com.morkim.tectonic;

import com.morkim.tectonic.entities.CancelledPrerequisiteTestUseCase;
import com.morkim.tectonic.entities.PendingActionRequest;
import com.morkim.tectonic.entities.PendingActionTestUseCase;
import com.morkim.tectonic.entities.TestResult;
import com.morkim.tectonic.entities.TestUseCase;

import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;

public class CancelTest {

	private long prerequisiteTimeStamp;
	private long mainTimeStamp;
	private int onCancelMainCount;
	private int onCancelPrerequisiteCount;

	@Before
	public void setup() {

		UseCase.unsubscribeAll();
		UseCase.clearAllInProgress();

		prerequisiteTimeStamp = 0;
		mainTimeStamp = 0;
		onCancelMainCount = 0;
		onCancelPrerequisiteCount = 0;
	}

	@Test
	public void cancelMain_onCancelCalled() throws Exception {

		TestUseCase useCase;

		useCase = new Main3TestUseCase();
		useCase.subscribe(new SimpleUseCaseListener<TestResult>() {
			@Override
			public void onComplete() {
				mainTimeStamp = System.nanoTime();
			}

			@Override
			public void onCancel() {
				onCancelMainCount++;
			}
		});
		useCase.execute();

		assertEquals(0, mainTimeStamp);
		assertEquals(1, onCancelMainCount);
	}

	@Test
	public void cancelMainTwice_onCancelCalledOnce() throws Exception {

		TestUseCase useCase;

		useCase = new Main3TestUseCase();
		useCase.subscribe(new SimpleUseCaseListener<TestResult>() {
			@Override
			public void onComplete() {
				mainTimeStamp = System.nanoTime();
			}

			@Override
			public void onCancel() {
				onCancelMainCount++;
			}
		});
		useCase.execute();

		UseCase.cancel(Main3TestUseCase.class);

		assertEquals(0, mainTimeStamp);
		assertEquals(1, onCancelMainCount);
	}

	@Test
	public void cancelPrerequisite_mainInProgress() throws Exception {

		TestUseCase useCase;

		useCase = new Main1TestUseCase();
		useCase.subscribe(new SimpleUseCaseListener<TestResult>() {
			@Override
			public void onComplete() {
				mainTimeStamp = System.nanoTime();
			}
		});
		useCase.execute();

		assertNotEquals(0, prerequisiteTimeStamp);
		assertEquals(0, mainTimeStamp);
	}

	@Test
	public void cancelPrerequisiteExternally_mainInProgress() throws Exception {

		TestUseCase useCase;

		useCase = new Main2TestUseCase();
		useCase.subscribe(new SimpleUseCaseListener<TestResult>() {
			@Override
			public void onComplete() {
				mainTimeStamp = System.nanoTime();
			}
		});
		useCase.execute();

		UseCase.cancel(PendingActionTestUseCase.class);

		assertNotEquals(0, prerequisiteTimeStamp);
		assertEquals(0, mainTimeStamp);
	}

	@Test
	public void cancelPrerequisiteTwice_onlyOneOnCancelCallback() throws Exception {

		TestUseCase useCase;

		useCase = new Main1TestUseCase();
		useCase.subscribe(new SimpleUseCaseListener<TestResult>() {
			@Override
			public void onComplete() {
				mainTimeStamp = System.nanoTime();
			}
		});
		useCase.execute();

		UseCase.cancel(CancelledPrerequisiteTestUseCase.class);

		assertEquals(1, onCancelPrerequisiteCount);
	}

	@Test
	public void cancelCompleted_onCancelNotCalled() throws Exception {

		TestUseCase useCase;

		useCase = new TestUseCase();
		useCase.subscribe(new SimpleUseCaseListener<TestResult>() {
			@Override
			public void onComplete() {
				mainTimeStamp = System.nanoTime();
			}

			@Override
			public void onCancel() {
				onCancelMainCount++;
			}
		});
		useCase.execute();

		UseCase.cancel(TestUseCase.class);

		assertEquals(0, onCancelMainCount);
	}

	@Test
	public void executeCancelledPrerequisite_mainContinueExecution() throws Exception {

		TestUseCase useCase;

		useCase = new Main2TestUseCase();
		useCase.subscribe(new SimpleUseCaseListener<TestResult>() {
			@Override
			public void onComplete() {
				mainTimeStamp = System.nanoTime();
			}
		});
		useCase.execute();

		UseCase.cancel(PendingActionTestUseCase.class);

		PendingActionTestUseCase prerequisiteUseCase = new PendingActionTestUseCase();
		prerequisiteUseCase.execute(new PendingActionRequest.Builder().build());

		assertNotEquals(0, mainTimeStamp);
	}

	private class Main1TestUseCase extends TestUseCase {

		@Override
		protected void onAddPrerequisites() {

			addPrerequisite(
					CancelledPrerequisiteTestUseCase.class,
					new SimpleUseCaseListener<TestResult>() {
						@Override
						public void onCancel() {
							prerequisiteTimeStamp = System.nanoTime();
							onCancelPrerequisiteCount++;
						}
					});
		}
	}

	private class Main2TestUseCase extends TestUseCase {

		@Override
		protected void onAddPrerequisites() {

			addPrerequisite(
					PendingActionTestUseCase.class,
					new SimpleUseCaseListener<TestResult>() {
						@Override
						public void onCancel() {
							prerequisiteTimeStamp = System.nanoTime();
							onCancelPrerequisiteCount++;
						}
					});
		}
	}

	private class Main3TestUseCase extends TestUseCase {

		@Override
		protected void onExecute(Request request) {
			cancel();
		}
	}
}
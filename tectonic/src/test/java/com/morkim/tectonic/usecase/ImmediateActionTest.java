package com.morkim.tectonic.usecase;

import com.morkim.tectonic.flow.Step;
import com.morkim.tectonic.usecase.entities.StepData;
import com.morkim.tectonic.usecase.entities.UndoUseCase;

import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ExecutionException;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class ImmediateActionTest extends ConcurrentTectonicTest {

    private Step stepP1 = new Step() {
        @Override
        public void terminate() {

        }
    };

    private Step stepS1 = new Step() {
        @Override
        public void terminate() {

        }
    };

    private Step stepP2 = new Step() {
        @Override
        public void terminate() {

        }
    };

    private Step stepP3 = new Step() {
        @Override
        public void terminate() {

        }
    };

    private Step stepS2 = new Step() {
        @Override
        public void terminate() {

        }
    };

    private List<Step> undoPrimarySteps = new ArrayList<>();
    private List<Boolean> undoPrimaryInclusive = new ArrayList<>();
    private List<Step> undoSecondarySteps = new ArrayList<>();

    private UUID ACTION_DATA_KEY_1 = UUID.randomUUID();
    private UUID ACTION_DATA_KEY_2 = UUID.randomUUID();
    private UUID ACTION_CONFIRMATION_KEY_1 = UUID.randomUUID();

    private UUID ACTION_DATA_KEY_3 = UUID.randomUUID();

    private UUID ACTION_DATA_KEY_4 = UUID.randomUUID();
    private UUID ACTION_DATA_KEY_5 = UUID.randomUUID();
    private UUID ACTION_CONFIRMATION_KEY_2 = UUID.randomUUID();

    private UUID ACTION_DATA_KEY_7 = UUID.randomUUID();
    private UUID ACTION_DATA_KEY_8 = UUID.randomUUID();
    private UUID ACTION_CONFIRMATION_KEY_3 = UUID.randomUUID();

    private UUID ACTION_DATA_KEY_9 = UUID.randomUUID();

    private boolean onAbortCalled;

    @Before
    public void setup() {
        super.setup();

        undoPrimarySteps.clear();
        undoPrimaryInclusive.clear();
        undoSecondarySteps.clear();

        onAbortCalled = false;
    }

    @Test
    public void undo_primary_actor__clears_to_previous_primary_step() throws Throwable {

        final StepData data1 = new StepData();
        final StepData data2 = new StepData();
        final StepData data3 = new StepData();
        final StepData data4 = new StepData();
        final StepData data5 = new StepData();
        final StepData data6 = new StepData();

        UndoUseCase useCase = UseCase.fetch(UndoUseCase.class);
        useCase.setPrimaryActor(new UndoPActor());
        useCase.setSecondaryActor(new UndoSActor());
        useCase.execute();

        replyPrimaryStep1(new Random<>(data1), new Random<>(data2));
        replySecondaryStep(ACTION_DATA_KEY_3, data3);
        undo();
        replyPrimaryStep1(new Random<>(data1), new Random<>(data2));
        replySecondaryStep(ACTION_DATA_KEY_3, data3);
        replyPrimaryStep2(new Random<>(data4), new Random<>(data5));
        replyPrimaryStep3(new Random<>(new StepData()), new Random<>(new StepData()));
        replySecondaryStep(ACTION_DATA_KEY_9, data6);

        waitForUseCaseToFinish();

        assertEquals(2, undoPrimarySteps.size());
        assertEquals(1, undoSecondarySteps.size());
        assertEquals(stepP2, undoPrimarySteps.get(0));
        assertTrue(undoPrimaryInclusive.get(0));
        assertEquals(stepS1, undoSecondarySteps.get(0));
    }

    private void undo() throws Throwable {

        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                sleep();
                useCaseHandle.undo();
            }
        });
        thread.start();

        thread.join();
    }

    private void replyPrimaryStep1(final Random<StepData> dataA, final Random<StepData> dataB) throws InterruptedException {
        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                sleep();
                useCaseHandle.replyWith(ACTION_DATA_KEY_1, dataA);
                useCaseHandle.replyWith(ACTION_DATA_KEY_2, dataB);
                useCaseHandle.replyWithRandom(ACTION_CONFIRMATION_KEY_1);
            }
        });
        thread.start();

        thread.join();
    }

    private void replyPrimaryStep2(final Random<StepData> dataA, final Random<StepData> dataB) throws InterruptedException {
        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                sleep();
                useCaseHandle.replyWith(ACTION_DATA_KEY_4, dataA);
                useCaseHandle.replyWith(ACTION_DATA_KEY_5, dataB);
                useCaseHandle.replyWithRandom(ACTION_CONFIRMATION_KEY_2);
            }
        });
        thread.start();

        thread.join();
    }

    private void replyPrimaryStep3(final Random<StepData> dataA, final Random<StepData> dataB) throws InterruptedException {
        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                sleep();
                useCaseHandle.replyWith(ACTION_DATA_KEY_7, dataA);
                useCaseHandle.replyWith(ACTION_DATA_KEY_8, dataB);
                useCaseHandle.replyWithRandom(ACTION_CONFIRMATION_KEY_3);
            }
        });
        thread.start();

        thread.join();
    }

    private void replySecondaryStep(final UUID key, final Object data) throws InterruptedException {
        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                sleep();
                useCaseHandle.replyWith(key, data);
            }
        });
        thread.start();

        thread.join();
    }

    private class UndoPActor implements UndoUseCase.PActor {

        @Override
        public void onStart(Integer event, UseCaseHandle handle) {
            useCaseHandle = handle;
        }

        @Override
        public void onComplete(Integer event) {

        }

        @Override
        public void onUndo(Step step, boolean inclusive) {
            undoPrimarySteps.add(step);
            undoPrimaryInclusive.add(inclusive);
        }

        @Override
        public void onAbort(Integer event) {
            onAbortCalled = true;
        }

        @Override
        public Random<StepData> requestData1() {
            return new Random<>(new StepData());
        }

        @Override
        public Random<StepData> requestData2() {
            return new Random<>(new StepData());
        }

        @Override
        public void requestConfirmation() {
            useCaseHandle.immediate(this, stepP1, ACTION_CONFIRMATION_KEY_1);
        }

        @Override
        public Random<StepData> requestData4() {
            return useCaseHandle.waitForRandom(ACTION_DATA_KEY_4);
        }

        @Override
        public Random<StepData> requestData5() {
            return useCaseHandle.waitForRandom(ACTION_DATA_KEY_5);
        }

        @Override
        public void requestAnotherConfirmation() throws UndoException, InterruptedException {
            useCaseHandle.waitForSafe(this, stepP2, ACTION_CONFIRMATION_KEY_2);
        }

        @Override
        public Random<StepData> requestData7() {
            return useCaseHandle.waitForRandom(ACTION_DATA_KEY_7);
        }

        @Override
        public Random<StepData> requestData8() {
            return useCaseHandle.waitForRandom(ACTION_DATA_KEY_8);
        }

        @Override
        public void requestYetAnotherConfirmation() throws InterruptedException, UndoException {
            useCaseHandle.waitForSafe(this, stepP3, ACTION_CONFIRMATION_KEY_3);
        }
    }

    private class UndoSActor implements UndoUseCase.SActor {

        @Override
        public void onStart(Integer event, UseCaseHandle handle) {
            useCaseHandle = handle;
        }

        @Override
        public void onComplete(Integer event) {

        }

        @Override
        public void onUndo(Step step, boolean inclusive) {
            undoSecondarySteps.add(step);
        }

        @Override
        public void onAbort(Integer event) {

        }

        @Override
        public StepData requestData3() throws InterruptedException, ExecutionException, UndoException {
            return useCaseHandle.waitFor(this, stepS1, ACTION_DATA_KEY_3);
        }

        @Override
        public StepData requestData9() throws InterruptedException, ExecutionException, UndoException {
            return useCaseHandle.waitFor(this, stepS2, ACTION_DATA_KEY_9);
        }
    }
}
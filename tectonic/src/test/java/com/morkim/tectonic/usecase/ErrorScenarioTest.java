package com.morkim.tectonic.usecase;

import com.morkim.tectonic.flow.Step;
import com.morkim.tectonic.usecase.entities.ErrorUseCase;
import com.morkim.tectonic.usecase.entities.StepData;

import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ExecutionException;

import static org.junit.Assert.assertEquals;

public class ErrorScenarioTest extends ConcurrentTectonicTest {

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
    private int requestConfirmation1;
    private int requestConfirmation2;
    private int requestConfirmation3;

    @Before
    public void setup() {
        super.setup();

        undoPrimarySteps.clear();
        undoPrimaryInclusive.clear();
        undoSecondarySteps.clear();

        requestConfirmation1 = 0;
        requestConfirmation2 = 0;
        requestConfirmation3 = 0;
    }

    @Test
    public void reset_first__clears_one_step() throws Throwable {

        final StepData data1 = new StepData();
        final StepData data2 = new StepData();
        final StepData data3 = new StepData();
        final StepData data4 = new StepData();
        final StepData data5 = new StepData();
        final StepData data6 = new StepData();

        ErrorUseCase useCase = UseCase.fetch(ErrorUseCase.class);
        useCase.setPrimaryActor(new UndoPActor());
        useCase.setSecondaryActor(new UndoSActor());
        useCase.execute();

        replyPrimaryStep1(new Random<>(data1), new Random<>(data2));
        replySecondaryStep(ACTION_DATA_KEY_3, data3);
        replyPrimaryStep2(new Random<>(data4), new Random<>(data5));
        replyPrimaryStep3(new Random<>(new StepData()), new Random<>(new StepData()));
        replySecondaryStep(ACTION_DATA_KEY_9, data6);

        waitForUseCaseToFinish();

        assertEquals(4, requestConfirmation1);
        assertEquals(3, requestConfirmation2);
        assertEquals(2, requestConfirmation3);
        assertEquals(3, data1.getAccessCount());
        assertEquals(3, data2.getAccessCount());
        assertEquals(3, data3.getAccessCount());
        assertEquals(2, data4.getAccessCount());
        assertEquals(2, data5.getAccessCount());
        assertEquals(1, data6.getAccessCount());
    }

    @Test
    public void reset_primary_actor__clears_one_step() throws Throwable {

        final StepData data1 = new StepData();
        final StepData data2 = new StepData();
        final StepData data3 = new StepData();
        final StepData data4 = new StepData();
        final StepData data5 = new StepData();
        final StepData data6 = new StepData();

        ErrorUseCase useCase = UseCase.fetch(ErrorUseCase.class);
        useCase.setPrimaryActor(new UndoPActor());
        useCase.setSecondaryActor(new UndoSActor());
        useCase.execute();

        replyPrimaryStep1(new Random<>(data1), new Random<>(data2));
        replySecondaryStep(ACTION_DATA_KEY_3, new Exception());
        replyPrimaryStep1(new Random<>(data1), new Random<>(data2));
        replySecondaryStep(ACTION_DATA_KEY_3, data3);
        replyPrimaryStep2(new Random<>(data4), new Random<>(data5));
        replyPrimaryStep3(new Random<>(new StepData()), new Random<>(new StepData()));
        replySecondaryStep(ACTION_DATA_KEY_9, data6);

        waitForUseCaseToFinish();

        assertEquals(6, requestConfirmation1);
        assertEquals(3, requestConfirmation2);
        assertEquals(2, requestConfirmation3);
        assertEquals(4, data1.getAccessCount());
        assertEquals(4, data2.getAccessCount());
        assertEquals(3, data3.getAccessCount());
        assertEquals(2, data4.getAccessCount());
        assertEquals(2, data5.getAccessCount());
        assertEquals(1, data6.getAccessCount());
    }

    @Test
    public void consecutive_primary_reset_primary_actor__clears_top_step() throws Throwable {

        final StepData data1 = new StepData();
        final StepData data2 = new StepData();
        final StepData data3 = new StepData();
        final StepData data4 = new StepData();
        final StepData data5 = new StepData();
        final StepData data6 = new StepData();

        ErrorUseCase useCase = UseCase.fetch(ErrorUseCase.class);
        useCase.setPrimaryActor(new UndoPActor());
        useCase.setSecondaryActor(new UndoSActor());
        useCase.execute();

        replyPrimaryStep1(new Random<>(data1), new Random<>(data2));
        replySecondaryStep(ACTION_DATA_KEY_3, data3);
        replyPrimaryStep2(new Random<>(data4), null);
        replyPrimaryStep2(new Random<>(data4), new Random<>(data5));
        replyPrimaryStep3(new Random<>(new StepData()), new Random<>(new StepData()));
        replySecondaryStep(ACTION_DATA_KEY_9, data6);

        waitForUseCaseToFinish();


        assertEquals(6, requestConfirmation1);
        assertEquals(5, requestConfirmation2);
        assertEquals(2, requestConfirmation3);
        assertEquals(5, data1.getAccessCount());
        assertEquals(5, data2.getAccessCount());
        assertEquals(5, data3.getAccessCount());
        assertEquals(3, data4.getAccessCount());
        assertEquals(2, data5.getAccessCount());
        assertEquals(1, data6.getAccessCount());
    }

    @Test
    public void reset_secondary_actor__clears_to_previous() throws Throwable {

        final StepData data1 = new StepData();
        final StepData data2 = new StepData();
        final StepData data3 = new StepData();
        final StepData data4 = new StepData();
        final StepData data5 = new StepData();
        final StepData data6 = new StepData();

        ErrorUseCase useCase = UseCase.fetch(ErrorUseCase.class);
        useCase.setPrimaryActor(new UndoPActor());
        useCase.setSecondaryActor(new UndoSActor());
        useCase.execute();

        replyPrimaryStep1(new Random<>(data1), null);
        replyPrimaryStep1(new Random<>(data1), new Random<>(data2));
        replySecondaryStep(ACTION_DATA_KEY_3, data3);
        replyPrimaryStep2(new Random<>(data4), new Random<>(data5));
        replyPrimaryStep3(new Random<>(new StepData()), new Random<>(new StepData()));
        replySecondaryStep(ACTION_DATA_KEY_9, data6);

        waitForUseCaseToFinish();

        assertEquals(6, requestConfirmation1);
        assertEquals(3, requestConfirmation2);
        assertEquals(2, requestConfirmation3);
        assertEquals(4, data1.getAccessCount());
        assertEquals(3, data2.getAccessCount());
        assertEquals(3, data3.getAccessCount());
        assertEquals(2, data4.getAccessCount());
        assertEquals(2, data5.getAccessCount());
        assertEquals(1, data6.getAccessCount());
    }

    @Test
    public void reset_cached__new_data_accessed_for_reset() throws Throwable {

        final StepData data1 = new StepData();
        final StepData data2 = new StepData();
        final StepData data3 = new StepData();
        final StepData data4 = new StepData();
        final StepData data5 = new StepData();
        final StepData data6 = new StepData();
        final StepData data1_ = new StepData();
        final StepData data2_ = new StepData();

        ErrorUseCase useCase = UseCase.fetch(ErrorUseCase.class);
        useCase.setPrimaryActor(new UndoPActor());
        useCase.setSecondaryActor(new UndoSActor());
        useCase.execute();

        replyPrimaryStep1(new Random<>(data1), new Random<>(data2));
        replySecondaryStep(ACTION_DATA_KEY_3, data3);
        replyPrimaryStep1(new Random<>(data1_), new Random<>(data2_));
        replySecondaryStep(ACTION_DATA_KEY_3, data3);
        replyPrimaryStep2(new Random<>(data4), new Random<>(data5));
        replyPrimaryStep3(new Random<>(new StepData()), new Random<>(new StepData()));
        replySecondaryStep(ACTION_DATA_KEY_9, data6);

        waitForUseCaseToFinish();

        assertEquals(1, data1.getAccessCount());
        assertEquals(1, data2.getAccessCount());
        assertEquals(4, data3.getAccessCount());
        assertEquals(2, data4.getAccessCount());
        assertEquals(2, data5.getAccessCount());
        assertEquals(1, data6.getAccessCount());
        assertEquals(3, data1_.getAccessCount());
        assertEquals(3, data2_.getAccessCount());
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

    private class UndoPActor implements ErrorUseCase.PActor {

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

        }

        @Override
        public Random<StepData> requestData1() {
            return useCaseHandle.waitForRandom(ACTION_DATA_KEY_1);
        }

        @Override
        public Random<StepData> requestData2() {
            return useCaseHandle.waitForRandom(ACTION_DATA_KEY_2);
        }

        @Override
        public void requestConfirmation() throws InterruptedException, UndoException {
            requestConfirmation1++;
            useCaseHandle.waitForSafe(this, stepP1, ACTION_CONFIRMATION_KEY_1);
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
            requestConfirmation2++;
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
            requestConfirmation3++;
            useCaseHandle.waitForSafe(this, stepP3, ACTION_CONFIRMATION_KEY_3);
        }

        @Override
        public void handleError(Exception e) {
            useCaseHandle.reset();
        }
    }

    private class UndoSActor implements ErrorUseCase.SActor {

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
package com.morkim.tectonic.usecase;

import com.morkim.tectonic.flow.Step;
import com.morkim.tectonic.usecase.entities.RandomActionsUseCase;
import com.morkim.tectonic.usecase.entities.StepData;

import org.junit.Before;
import org.junit.Test;

import java.util.UUID;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class RandomAccessActorTest extends ConcurrentTectonicTest {

    private UUID ACTION_DATA_KEY_1 = UUID.randomUUID();
    private UUID ACTION_DATA_KEY_2 = UUID.randomUUID();
    private UUID ACTION_DATA_KEY_3 = UUID.randomUUID();
    private UUID CONFIRM = UUID.randomUUID();

    private int count;

    @Before
    public void setup() {
        super.setup();

        count = 0;
    }

    @Test
    public void user_confirms_all_once() throws InterruptedException {

        final Step step = new Step() {
            @Override
            public void terminate() {

            }
        };

        final StepData data1 = new StepData();
        final StepData data2 = new StepData();
        final StepData data3 = new StepData();

        RandomActionsUseCase useCase = UseCase.fetch(RandomActionsUseCase.class);
        RandomActionsUseCase.Actor actor = new RandomActionsUseCase.Actor() {

            @Override
            public void onStart(Object event, UseCaseHandle handle) {
                useCaseHandle = handle;
            }

            @Override
            public void onComplete(Object event, Object result) {

            }

            @Override
            public void onAbort(Object event) {

            }

            @Override
            public void onUndo(Step step, boolean inclusive) {

            }

            @Override
            public Random<StepData> requestData1() {
                count++;
                return useCaseHandle.waitForRandom(ACTION_DATA_KEY_1);
            }

            @Override
            public Random<StepData> requestData2() {
                count++;
                return useCaseHandle.waitForRandom(ACTION_DATA_KEY_2);
            }

            @Override
            public Random<StepData> requestData3() {
                count++;
                return useCaseHandle.waitForRandom(ACTION_DATA_KEY_3);
            }

            @Override
            public void confirm() throws InterruptedException, UndoException {
                count++;
                useCaseHandle.waitForSafe(this, step, CONFIRM);
            }
        };

        useCase.setActor(actor);

        useCase.execute();

        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                sleep();
                useCaseHandle.replyWith(ACTION_DATA_KEY_1, new Random<>(data1));
                useCaseHandle.replyWith(ACTION_DATA_KEY_2, new Random<>(data2));
                useCaseHandle.replyWith(ACTION_DATA_KEY_3, new Random<>(data3));
                useCaseHandle.replyWithRandom(CONFIRM);
            }
        });
        thread.start();

        thread.join();

        useCaseThread.join();

        assertTrue(useCase.isData1Validated());
        assertTrue(useCase.isData2Validated());
        assertTrue(useCase.isData3Validated());

        assertEquals(1, data1.getAccessCount());
        assertEquals(1, data2.getAccessCount());
        assertEquals(1, data3.getAccessCount());
        assertEquals(8, count);
    }

//    @Test
//    public void user_submits_3_1_2() throws InterruptedException {
//
//        final StepData data1 = new StepData();
//        final StepData data2 = new StepData();
//        final StepData data3 = new StepData();
//
//        RandomActionsUseCase useCase = UseCase.fetch(RandomActionsUseCase.class);
//        RandomActionsUseCase.SimpleActor actor = new RandomActionsUseCase.SimpleActor() {
//
//            @Override
//            public Random<StepData> requestData1() throws InterruptedException {
//                count++;
//                return UseCase.waitForRandom(ACTION_DATA_KEY_1);
//            }
//
//            @Override
//            public Random<StepData> requestData2() throws InterruptedException {
//                count++;
//                return UseCase.waitForRandom(ACTION_DATA_KEY_2);
//            }
//
//            @Override
//            public Random<StepData> requestData3() throws InterruptedException {
//                count++;
//                return UseCase.waitForRandom(ACTION_DATA_KEY_3);
//            }
//        };
//
//        useCase.setUnknownActor(actor);
//
//        useCase.execute();
//
//        Thread thread1 = new Thread(new Runnable() {
//            @Override
//            public void run() {
//                sleep();
//                UseCase.replyWith(ACTION_DATA_KEY_3, new Random<>(data3));
//            }
//        });
//        thread1.start();
//
//        thread1.join();
//
//        sleep();
//
//        assertFalse(useCase.isData1Validated());
//        assertFalse(useCase.isData2Validated());
//        assertTrue(useCase.isData3Validated());
//
//        Thread thread2 = new Thread(new Runnable() {
//            @Override
//            public void run() {
//                sleep();
//                UseCase.replyWith(ACTION_DATA_KEY_1, new Random<>(data1));
//            }
//        });
//        thread2.start();
//
//        thread2.join();
//
//        sleep();
//
//        assertTrue(useCase.isData1Validated());
//        assertFalse(useCase.isData2Validated());
//        assertTrue(useCase.isData3Validated());
//
//        Thread thread3 = new Thread(new Runnable() {
//            @Override
//            public void run() {
//                sleep();
//                UseCase.replyWith(ACTION_DATA_KEY_2, new Random<>(data2));
//            }
//        });
//        thread3.start();
//
//        thread3.join();
//
//        useCaseThread.join();
//
//        assertTrue(useCase.isData1Validated());
//        assertTrue(useCase.isData2Validated());
//        assertTrue(useCase.isData3Validated());
//
//        assertEquals(1, data1.getAccessCount());
//        assertEquals(1, data2.getAccessCount());
//        assertEquals(2, data3.getAccessCount());
//        assertEquals(6, count);
//    }
}
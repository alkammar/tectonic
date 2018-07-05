package com.morkim.tectonic.usecase;

import com.morkim.tectonic.usecase.entities.RandomActionsUseCase;
import com.morkim.tectonic.usecase.entities.StepData;

import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class RandomAccessActorTest extends ConcurrentTectonicTest {

    private int ACTION_DATA_KEY_1 = 1;
    private int ACTION_DATA_KEY_2 = 2;
    private int ACTION_DATA_KEY_3 = 3;
    private int CONFIRM = 4;

    private int count;

    @Before
    public void setup() {
        super.setup();

        count = 0;
    }

    @Test
    public void user_confirms_all_once() throws InterruptedException {

        final StepData data1 = new StepData();
        final StepData data2 = new StepData();
        final StepData data3 = new StepData();

        RandomActionsUseCase useCase = UseCase.fetch(RandomActionsUseCase.class);
        RandomActionsUseCase.Actor actor = new RandomActionsUseCase.Actor() {

            @Override
            public Random<StepData> requestData1() throws InterruptedException {
                count++;
                return UseCase.waitForRandom(ACTION_DATA_KEY_1, new StepData());
            }

            @Override
            public Random<StepData> requestData2() throws InterruptedException {
                count++;
                return UseCase.waitForRandom(ACTION_DATA_KEY_2, new StepData());
            }

            @Override
            public Random<StepData> requestData3() throws InterruptedException {
                count++;
                return UseCase.waitForRandom(ACTION_DATA_KEY_3, new StepData());
            }

            @Override
            public void confirm() throws InterruptedException {
                count++;
                UseCase.waitFor(CONFIRM);
            }
        };

        useCase.setActor(actor);

        useCase.execute();

        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                sleep();
                UseCase.replyWith(ACTION_DATA_KEY_1, new Random<>(data1));
                UseCase.replyWith(ACTION_DATA_KEY_2, new Random<>(data2));
                UseCase.replyWith(ACTION_DATA_KEY_3, new Random<>(data3));
                UseCase.replyWithRandom(CONFIRM);
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
//        RandomActionsUseCase.Actor actor = new RandomActionsUseCase.Actor() {
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
//        useCase.setActor(actor);
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
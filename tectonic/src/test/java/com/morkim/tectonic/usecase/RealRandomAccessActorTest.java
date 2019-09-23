package com.morkim.tectonic.usecase;

import com.morkim.tectonic.flow.Step;
import com.morkim.tectonic.usecase.entities.RealRandomActionsUseCase;
import com.morkim.tectonic.usecase.entities.StepData;

import org.junit.Before;
import org.junit.Test;

import java.util.UUID;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class RealRandomAccessActorTest extends ConcurrentTectonicTest {

    private UUID ACTION_DATA_KEY_1 = UUID.randomUUID();
    private UUID ACTION_DATA_KEY_2 = UUID.randomUUID();
    private UUID ACTION_DATA_KEY_3 = UUID.randomUUID();

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

        RealRandomActionsUseCase useCase = UseCase.fetch(RealRandomActionsUseCase.class);
        RealRandomActionsUseCase.Actor actor = new RealRandomActionsUseCase.Actor() {

            @Override
            public void onStart(Object event, UseCaseHandle handle) {
                useCaseHandle = handle;
            }

            @Override
            public void onComplete(Object event) {

            }

            @Override
            public void onAbort(Object event) {

            }

            @Override
            public void onUndo(Step step, boolean inclusive) {

            }

            @Override
            public StepData requestData1() throws UndoException, InterruptedException {
                count++;
                return useCaseHandle.waitForSafe(this, ACTION_DATA_KEY_1);
            }

            @Override
            public StepData requestData2() throws UndoException, InterruptedException {
                count++;
                return useCaseHandle.waitForSafe(this, ACTION_DATA_KEY_2);
            }

            @Override
            public StepData requestData3() throws UndoException, InterruptedException {
                count++;
                return useCaseHandle.waitForSafe(this, ACTION_DATA_KEY_3);
            }
        };

        useCase.setActor(actor);

        useCase.execute();

        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                sleep();
                useCaseHandle.replyWith(ACTION_DATA_KEY_3, data3);
            }
        });
        thread.start();

        thread.join();

//        useCaseThread.join();

        Thread thread2 = new Thread(new Runnable() {
            @Override
            public void run() {
                sleep();
                useCaseHandle.replyWith(ACTION_DATA_KEY_1, data1);
            }
        });
        thread2.start();

        thread2.join();

//        useCaseThread.join();

        Thread thread3 = new Thread(new Runnable() {
            @Override
            public void run() {
                sleep();
                useCaseHandle.replyWith(ACTION_DATA_KEY_2, data2);
            }
        });
        thread3.start();

        thread3.join();

        useCaseThread.join();

//        assertTrue(useCase.isData1Validated());
//        assertTrue(useCase.isData2Validated());
//        assertTrue(useCase.isData3Validated());

        assertEquals(1, data1.getAccessCount());
        assertEquals(1, data2.getAccessCount());
        assertEquals(1, data3.getAccessCount());
        assertEquals(4, count);
    }
}
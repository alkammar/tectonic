package com.morkim.tectonic.usecase;

import com.morkim.tectonic.flow.Step;
import com.morkim.tectonic.usecase.entities.StepData;
import com.morkim.tectonic.usecase.entities.UndoUseCase;

import org.junit.Before;
import org.junit.Test;

import java.util.UUID;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class UndoTest extends ConcurrentTectonicTest {

    private Step step1 = new Step() {
        @Override
        public void terminate() {

        }
    };

    private Step step2 = new Step() {
        @Override
        public void terminate() {

        }
    };

    private UUID ACTION_DATA_KEY_1 = UUID.randomUUID();
    private UUID ACTION_DATA_KEY_2 = UUID.randomUUID();
    private UUID ACTION_DATA_KEY_3 = UUID.randomUUID();

    private int count;

    private boolean onUndoCalled;

    @Before
    public void setup() {
        super.setup();

        count = 0;

        onUndoCalled = false;
    }

    @Test
    public void undo_cached__new_data_accessed_for_undone() throws InterruptedException {

        final StepData data1 = new StepData();
        final StepData data2 = new StepData();
        final StepData data3 = new StepData();
        final StepData data4 = new StepData();

        UndoUseCase useCase = UseCase.fetch(UndoUseCase.class);
        UndoUseCase.Actor actor = new UndoUseCase.Actor() {

            @Override
            public void onStart(Integer event, UseCaseHandle handle) {
                useCaseHandle = handle;
            }

            @Override
            public void onComplete(Integer event, Void result) {

            }

            @Override
            public void onUndo(Step step) {
                onUndoCalled = true;
            }

            @Override
            public void onAbort(Integer event) {

            }

            @Override
            public StepData requestData() throws InterruptedException, UndoException {
                count++;
                return useCaseHandle.waitForSafe(ACTION_DATA_KEY_1);
            }

            @Override
            public StepData requestOtherData() throws InterruptedException, UndoException {
                count++;
                return useCaseHandle.waitForSafe(ACTION_DATA_KEY_2);
            }

            @Override
            public StepData requestAnotherData() throws InterruptedException, UndoException {
                count++;
                return useCaseHandle.waitForSafe(ACTION_DATA_KEY_3);
            }
        };
        useCase.addPrimaryActor(actor);
        useCase.setActor(actor);
        useCase.execute();
//
        Thread thread1 = new Thread(new Runnable() {
            @Override
            public void run() {
                sleep();
                useCaseHandle.replyWith(step1, ACTION_DATA_KEY_1, data1);
            }
        });
        thread1.start();

        thread1.join();

        Thread thread2 = new Thread(new Runnable() {
            @Override
            public void run() {
                sleep();
                useCaseHandle.replyWith(step2, ACTION_DATA_KEY_2, data2);
            }
        });
        thread2.start();

        thread2.join();

        Thread thread3 = new Thread(new Runnable() {
            @Override
            public void run() {
                sleep();
                useCaseHandle.undo(step2);
            }
        });
        thread3.start();

        thread3.join();

        Thread thread4 = new Thread(new Runnable() {
            @Override
            public void run() {
                sleep();
                useCaseHandle.replyWith(step2, ACTION_DATA_KEY_2, data4);
            }
        });
        thread4.start();

        thread4.join();

        Thread thread5 = new Thread(new Runnable() {
            @Override
            public void run() {
                sleep();
                useCaseHandle.replyWith(step2, ACTION_DATA_KEY_3, data3);
            }
        });
        thread5.start();

        thread5.join();

        useCaseThread.join();

        assertEquals(2, data1.getAccessCount());
        assertEquals(1, data2.getAccessCount());
        assertEquals(1, data3.getAccessCount());
        assertEquals(1, data4.getAccessCount());
        assertEquals(6, count);
        assertTrue(onUndoCalled);
    }
}
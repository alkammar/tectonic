package com.morkim.tectonic.usecase;

import com.morkim.tectonic.flow.Step;
import com.morkim.tectonic.usecase.entities.UndoUseCase;
import com.morkim.tectonic.usecase.entities.StepData;

import org.junit.Before;
import org.junit.Test;

import java.util.UUID;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class UndoTest extends ConcurrentTectonicTest {

    private UUID ACTION_DATA_KEY_1 = UUID.randomUUID();
    private UUID ACTION_DATA_KEY_2 = UUID.randomUUID();
    private UUID ACTION_DATA_KEY_3 = UUID.randomUUID();

    private UseCaseHandle handle;

    private int count;

    private boolean onUndoCalled;

    @Before
    public void setup() {
        super.setup();

        count = 0;

        handle = null;

        onUndoCalled = false;
    }

    @Test
    public void undo_non_cached__actor_data_accessed() throws InterruptedException {

        final Step step = new Step() {
            @Override
            public void terminate() {

            }
        };

        final StepData data1 = new StepData();
        final StepData data2 = new StepData();
        final StepData data3 = new StepData();

        UndoUseCase useCase = UseCase.fetch(UndoUseCase.class);
        UndoUseCase.Actor actor = new UndoUseCase.Actor() {

            @Override
            public void onStart(UseCaseHandle handle) {
                UndoTest.this.handle = handle;
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
            public StepData requestData() throws InterruptedException {
                count++;
                return UseCase.immediate(data1);
            }

            @Override
            public StepData requestOtherData() throws InterruptedException {
                count++;
                return UseCase.immediate(data2);
            }

            @Override
            public StepData requestAnotherData() throws InterruptedException {
                if (count < 3) handle.undo(step, ACTION_DATA_KEY_2);
                return UseCase.immediate(data3);
            }
        };

        useCase.setPrimaryActor(actor);
        useCase.setActor(actor);
        useCase.execute();

        useCaseThread.join();

        assertEquals(2, data1.getAccessCount());
        assertEquals(2, data2.getAccessCount());
        assertEquals(1, data3.getAccessCount());
        assertTrue(onUndoCalled);
    }

    @Test
    public void undo_cached__new_data_accessed_for_undone() throws InterruptedException {

        final Step step = new Step() {
            @Override
            public void terminate() {

            }
        };

        final StepData data1 = new StepData();
        final StepData data2 = new StepData();
        final StepData data3 = new StepData();
        final StepData data4 = new StepData();

        UndoUseCase useCase = UseCase.fetch(UndoUseCase.class);
        UndoUseCase.Actor actor = new UndoUseCase.Actor() {

            @Override
            public void onStart(UseCaseHandle handle) {
                UndoTest.this.handle = handle;
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
            public StepData requestData() throws InterruptedException {
                count++;
                return UseCase.waitForSafe(ACTION_DATA_KEY_1);
            }

            @Override
            public StepData requestOtherData() throws InterruptedException {
                count++;
                return UseCase.waitForSafe(ACTION_DATA_KEY_2);
            }

            @Override
            public StepData requestAnotherData() throws InterruptedException {
                count++;
                return UseCase.waitForSafe(ACTION_DATA_KEY_3);
            }
        };
        useCase.setPrimaryActor(actor);
        useCase.setActor(actor);
        useCase.execute();
//
        Thread thread1 = new Thread(new Runnable() {
            @Override
            public void run() {
                sleep();
                UseCase.replyWith(ACTION_DATA_KEY_1, data1);
            }
        });
        thread1.start();

        thread1.join();

        Thread thread2 = new Thread(new Runnable() {
            @Override
            public void run() {
                sleep();
                UseCase.replyWith(ACTION_DATA_KEY_2, data2);
            }
        });
        thread2.start();

        thread2.join();

        Thread thread3 = new Thread(new Runnable() {
            @Override
            public void run() {
                sleep();
                handle.undo(step, ACTION_DATA_KEY_2);
            }
        });
        thread3.start();

        thread3.join();

        Thread thread4 = new Thread(new Runnable() {
            @Override
            public void run() {
                sleep();
                UseCase.replyWith(ACTION_DATA_KEY_2, data4);
            }
        });
        thread4.start();

        thread4.join();

        Thread thread5 = new Thread(new Runnable() {
            @Override
            public void run() {
                sleep();
                UseCase.replyWith(ACTION_DATA_KEY_3, data3);
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
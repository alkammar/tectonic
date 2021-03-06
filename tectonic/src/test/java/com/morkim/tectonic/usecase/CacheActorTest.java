package com.morkim.tectonic.usecase;

import com.morkim.tectonic.flow.Step;
import com.morkim.tectonic.usecase.entities.MultipleActionUseCase;
import com.morkim.tectonic.usecase.entities.StepData;

import org.junit.Before;
import org.junit.Test;

import java.util.UUID;

import static org.junit.Assert.assertEquals;

public class CacheActorTest extends ConcurrentTectonicTest {

    Step step = new Step() {
        @Override
        public void terminate() {

        }
    };

    private UUID ACTION_DATA_KEY_1 = UUID.randomUUID();
    private UUID ACTION_DATA_KEY_2 = UUID.randomUUID();

    private int count;

    @Before
    public void setup() {
        super.setup();

        count = 0;
    }

    @Test
    public void early_second_action__cached_data_accessed() throws InterruptedException {

        final StepData data1 = new StepData();
        final StepData data2 = new StepData();

        MultipleActionUseCase useCase = UseCase.fetch(MultipleActionUseCase.class);
        MultipleActionUseCase.Actor actor = new MultipleActionUseCase.Actor() {

            @Override
            public void onComplete(Object event) {

            }

            @Override
            public void onAbort(Object event) {

            }

            @Override
            public void onStart(Object event, UseCaseHandle handle) {
                useCaseHandle = handle;
            }

            @Override
            public void onUndo(Step step, boolean inclusive) {

            }

            @Override
            public StepData requestData1() throws InterruptedException, UndoException {
                count++;
                return useCaseHandle.waitForSafe(this, step, ACTION_DATA_KEY_1);
            }

            @Override
            public StepData requestData2() throws InterruptedException, UndoException {
                count++;
                return useCaseHandle.waitForSafe(this, step, ACTION_DATA_KEY_2);
            }
        };

        useCase.setActor(actor);

        useCase.execute();

        Thread thread1 = new Thread(new Runnable() {
            @Override
            public void run() {
                sleep();
                useCaseHandle.replyWith(ACTION_DATA_KEY_2, data2);
            }
        });
        thread1.start();

        thread1.join();

        Thread thread2 = new Thread(new Runnable() {
            @Override
            public void run() {
                sleep();
                useCaseHandle.replyWith(ACTION_DATA_KEY_1, data1);
            }
        });
        thread2.start();

        thread2.join();

        waitForUseCaseToFinish();

        assertEquals(1, data1.getAccessCount());
        assertEquals(1, data2.getAccessCount());
        assertEquals(3, count);
    }

    @Test
    public void duplicate_first_action__cached_data_accessed() throws InterruptedException {

        final StepData data1 = new StepData();
        final StepData data2 = new StepData();
        final StepData data3 = new StepData();

        MultipleActionUseCase useCase = UseCase.fetch(MultipleActionUseCase.class);
        MultipleActionUseCase.Actor actor = new MultipleActionUseCase.Actor() {

            @Override
            public void onComplete(Object event) {

            }

            @Override
            public void onAbort(Object event) {

            }

            @Override
            public void onStart(Object event, UseCaseHandle handle) {
                useCaseHandle = handle;
            }

            @Override
            public void onUndo(Step step, boolean inclusive) {

            }

            @Override
            public StepData requestData1() throws InterruptedException, UndoException {
                count++;
                return useCaseHandle.waitForSafe(this, step, ACTION_DATA_KEY_1);
            }

            @Override
            public StepData requestData2() throws InterruptedException, UndoException {
                count++;
                return useCaseHandle.waitForSafe(this, step, ACTION_DATA_KEY_2);
            }
        };

        useCase.setActor(actor);

        useCase.execute();

        Thread thread1 = new Thread(new Runnable() {
            @Override
            public void run() {
                sleep();
                useCaseHandle.replyWith(ACTION_DATA_KEY_1, data1);
            }
        });
        thread1.start();

        thread1.join();

        Thread thread2 = new Thread(new Runnable() {
            @Override
            public void run() {
                sleep();
                useCaseHandle.replyWith(ACTION_DATA_KEY_1, data2);
            }
        });
        thread2.start();

        thread2.join();

        Thread thread3 = new Thread(new Runnable() {
            @Override
            public void run() {
                sleep();
                useCaseHandle.replyWith(ACTION_DATA_KEY_2, data3);
            }
        });
        thread3.start();

        thread3.join();

        waitForUseCaseToFinish();

        assertEquals(1, data1.getAccessCount());
        assertEquals(1, data2.getAccessCount());
        assertEquals(1, data3.getAccessCount());
        assertEquals(4, count);
    }
}
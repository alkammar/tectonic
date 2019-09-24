package com.morkim.tectonic.usecase;

import com.morkim.tectonic.flow.Step;
import com.morkim.tectonic.usecase.entities.RealRandomActionsUseCase;
import com.morkim.tectonic.usecase.entities.StepData;

import org.junit.Before;
import org.junit.Test;

import java.util.UUID;

import static org.junit.Assert.assertEquals;

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
    public void submit_in_order() throws InterruptedException {

        final StepData data1 = new StepData();
        final StepData data2 = new StepData();
        final StepData data3 = new StepData();

        RealRandomActionsUseCase useCase = UseCase.fetch(RealRandomActionsUseCase.class);
        RealRandomActionsUseCase.Actor actor = createActor();

        useCase.setActor(actor);

        useCase.execute();

        submit(ACTION_DATA_KEY_1, data1, 100);
        submit(ACTION_DATA_KEY_2, data2, 100);
        submit(ACTION_DATA_KEY_3, data3, 100);

        waitForUseCaseToFinish();

        assertEquals(1, data1.getAccessCount());
        assertEquals(1, data2.getAccessCount());
        assertEquals(1, data3.getAccessCount());
        assertEquals(3, count);
    }

    @Test
    public void submit_in_order_without_delay() throws InterruptedException {

        final StepData data1 = new StepData();
        final StepData data2 = new StepData();
        final StepData data3 = new StepData();

        RealRandomActionsUseCase useCase = UseCase.fetch(RealRandomActionsUseCase.class);
        RealRandomActionsUseCase.Actor actor = createActor();

        useCase.setActor(actor);

        useCase.execute();

        submit(ACTION_DATA_KEY_1, data1);
        submit(ACTION_DATA_KEY_2, data2);
        submit(ACTION_DATA_KEY_3, data3);

        waitForUseCaseToFinish();

        assertEquals(1, data1.getAccessCount());
        assertEquals(1, data2.getAccessCount());
        assertEquals(1, data3.getAccessCount());
        assertEquals(3, count);
    }

    @Test
    public void submit_third_before_first__able_to_submit_remaining() throws InterruptedException {

        final StepData data1 = new StepData();
        final StepData data2 = new StepData();
        final StepData data3 = new StepData();

        RealRandomActionsUseCase useCase = UseCase.fetch(RealRandomActionsUseCase.class);
        RealRandomActionsUseCase.Actor actor = createActor();

        useCase.setActor(actor);

        useCase.execute();

        submit(ACTION_DATA_KEY_3, data3, 100);
        submit(ACTION_DATA_KEY_1, data1, 100);
        submit(ACTION_DATA_KEY_2, data2, 100);

        waitForUseCaseToFinish();

        assertEquals(1, data1.getAccessCount());
        assertEquals(1, data2.getAccessCount());
        assertEquals(1, data3.getAccessCount());
        assertEquals(4, count);
    }

    @Test
    public void submit_first_again__able_to_submit_remaining() throws InterruptedException {

        final StepData data1 = new StepData();
        final StepData data2 = new StepData();
        final StepData data3 = new StepData();
        final StepData data1_2 = new StepData();

        RealRandomActionsUseCase useCase = UseCase.fetch(RealRandomActionsUseCase.class);
        RealRandomActionsUseCase.Actor actor = createActor();

        useCase.setActor(actor);

        useCase.execute();

        submit(ACTION_DATA_KEY_1, data1, 100);
        submit(ACTION_DATA_KEY_2, data2, 100);
        submit(ACTION_DATA_KEY_1, data1_2, 100);
        submit(ACTION_DATA_KEY_3, data3, 100);

        waitForUseCaseToFinish();

        assertEquals(0, data1.getAccessCount());
        assertEquals(1, data2.getAccessCount());
        assertEquals(1, data3.getAccessCount());
        assertEquals(1, data1_2.getAccessCount());
        assertEquals(6, count);
    }

    private void submit(final UUID uuid, final StepData data) throws InterruptedException {
        submit(uuid, data, 0);
    }

    private void submit(final UUID uuid, final StepData data, final int delay) throws InterruptedException {
        Thread thread = new Thread(new Runnable() {

            @Override
            public void run() {
                if (delay > 0) sleep(delay);
                useCaseHandle.replyWith(uuid, data);
            }
        });
        thread.start();
        thread.join();
    }

    private RealRandomActionsUseCase.Actor createActor() {
        return new RealRandomActionsUseCase.Actor() {

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
    }
}
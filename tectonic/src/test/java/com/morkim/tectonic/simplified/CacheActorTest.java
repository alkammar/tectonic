package com.morkim.tectonic.simplified;

import com.morkim.tectonic.simplified.entities.ActorCachingUseCase;
import com.morkim.tectonic.simplified.entities.MultipleActionUseCase;
import com.morkim.tectonic.simplified.entities.StepData;

import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class CacheActorTest extends ConcurrentTectonicTest {

    private int ACTION_DATA_KEY_1 = 1;
    private int ACTION_DATA_KEY_2 = 2;

    private int count;

    @Before
    public void setup() {
        super.setup();

        count = 0;
    }

    @Test
    public void no_caching__new_data_accessed() throws InterruptedException {

        final StepData data1 = new StepData();
        final StepData data2 = new StepData();

        ActorCachingUseCase useCase = UseCase.fetch(ActorCachingUseCase.class);
        ActorCachingUseCase.Actor actor = new ActorCachingUseCase.Actor() {

            @Override
            public StepData requestData() {
                count++;
                return UseCase.immediate(count == 1 ? data1 : data2);
            }
        };
        useCase.setActor(actor);
        useCase.execute();

        sleep();

        useCase.restart();

        useCaseThread.join();

        assertEquals(1, data1.getAccessCount());
        assertEquals(1, data2.getAccessCount());
        assertEquals(2, count);
    }

    @Test
    public void cache_data__cached_data_accessed() throws InterruptedException {

        final StepData data1 = new StepData();
        final StepData data2 = new StepData();

        ActorCachingUseCase useCase = UseCase.fetch(ActorCachingUseCase.class);
        ActorCachingUseCase.Actor actor = new ActorCachingUseCase.Actor() {

            @Override
            public StepData requestData() throws InterruptedException {
                count++;
                return UseCase.waitFor(ACTION_DATA_KEY_1);
            }
        };
        useCase.setActor(actor);

        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                sleep();
                UseCase.replyWith(ACTION_DATA_KEY_1, data1);
            }
        });
        thread.start();

        useCase.execute();

        thread.join();

        useCase.restart();

        sleep();

        assertEquals(2, data1.getAccessCount());
        assertEquals(0, data2.getAccessCount());
        assertEquals(2, count);
    }

    @Test
    public void early_second_action__cached_data_accessed() throws InterruptedException {

        final StepData data1 = new StepData();
        final StepData data2 = new StepData();

        MultipleActionUseCase useCase = UseCase.fetch(MultipleActionUseCase.class);
        MultipleActionUseCase.Actor actor = new MultipleActionUseCase.Actor() {

            @Override
            public StepData requestData1() throws InterruptedException {
                count++;
                return UseCase.waitFor(ACTION_DATA_KEY_1);
            }

            @Override
            public StepData requestData2() throws InterruptedException {
                count++;
                return UseCase.waitFor(ACTION_DATA_KEY_2);
            }
        };

        useCase.setActor(actor);

        useCase.execute();

        Thread thread1 = new Thread(new Runnable() {
            @Override
            public void run() {
                sleep();
                UseCase.replyWith(ACTION_DATA_KEY_2, data2);
            }
        });
        thread1.start();

        thread1.join();

        Thread thread2 = new Thread(new Runnable() {
            @Override
            public void run() {
                sleep();
                UseCase.replyWith(ACTION_DATA_KEY_1, data1);
            }
        });
        thread2.start();

        thread2.join();

        useCaseThread.join();

        assertEquals(1, data1.getAccessCount());
        assertEquals(1, data2.getAccessCount());
        assertEquals(2, count);
    }

    @Test
    public void duplicate_first_action__cached_data_accessed() throws InterruptedException {

        final StepData data1 = new StepData();
        final StepData data2 = new StepData();
        final StepData data3 = new StepData();

        MultipleActionUseCase useCase = UseCase.fetch(MultipleActionUseCase.class);
        MultipleActionUseCase.Actor actor = new MultipleActionUseCase.Actor() {

            @Override
            public StepData requestData1() throws InterruptedException {
                count++;
                return UseCase.waitFor(ACTION_DATA_KEY_1);
            }

            @Override
            public StepData requestData2() throws InterruptedException {
                count++;
                return UseCase.waitFor(ACTION_DATA_KEY_2);
            }
        };

        useCase.setActor(actor);

        useCase.execute();

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
                UseCase.replyWith(ACTION_DATA_KEY_1, data2);
            }
        });
        thread2.start();

        thread2.join();

        Thread thread3 = new Thread(new Runnable() {
            @Override
            public void run() {
                sleep();
                UseCase.replyWith(ACTION_DATA_KEY_2, data3);
            }
        });
        thread3.start();

        thread3.join();

        useCaseThread.join();

        assertEquals(2, data1.getAccessCount());
        assertEquals(0, data2.getAccessCount());
        assertEquals(1, data3.getAccessCount());
        assertEquals(4, count);
    }
}
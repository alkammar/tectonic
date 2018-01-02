package com.morkim.tectonic;

import android.support.annotation.NonNull;

import com.morkim.tectonic.entities.PendingActionRequest;
import com.morkim.tectonic.entities.RequestActionTestUseCase;

import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import java.util.List;
import java.util.concurrent.Callable;

import io.reactivex.Scheduler;
import io.reactivex.android.plugins.RxAndroidPlugins;
import io.reactivex.functions.Function;
import io.reactivex.plugins.RxJavaPlugins;
import io.reactivex.schedulers.Schedulers;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class ActionRequiredTest extends TecTonicTest {

    private int actor1Action;
    private int actor2Action;

    @BeforeClass
    public static void setupClass() {

        RxAndroidPlugins.setInitMainThreadSchedulerHandler(new Function<Callable<Scheduler>, Scheduler>() {
            @Override
            public Scheduler apply(@NonNull Callable<Scheduler> schedulerCallable) throws Exception {
                return Schedulers.trampoline();
            }
        });

        RxJavaPlugins.setIoSchedulerHandler(new Function<Scheduler, Scheduler>() {
            @Override
            public Scheduler apply(@io.reactivex.annotations.NonNull Scheduler scheduler) throws Exception {
                return Schedulers.trampoline();
            }
        });

        UseCase.setLooperConfigs(UseCase.STUB_LOOPER_CHECKER);
    }

    @Before
    public void setup() {

        UseCase.unsubscribeAll();
        UseCase.clearAllInProgress();

        actor1Action = 0;
        actor2Action = 0;
    }

    @Test
    public void executeFirstTimeMissingInput_executionContinuesWhenInputsAreComplete() throws Exception {

        RequestActionTestUseCase

                useCase = UseCase.fetch(RequestActionTestUseCase.class);
        useCase.execute(new PendingActionRequest.Builder()
                .input1("asdfas")
                .input2("")
                .build());

        useCase = UseCase.fetch(RequestActionTestUseCase.class);
        useCase.execute(new PendingActionRequest.Builder()
                .input1("asdfas")
                .input2("qw98ewu9j")
                .build());

        assertTrue(useCase.isExecutionContinued());
    }

    @Test
    public void executeMultiple_onlyNewSubscriptionOnStartAndOnExecute() throws Exception {

        RequestActionTestUseCase
                useCase = UseCase.fetch(RequestActionTestUseCase.class);
        useCase.subscribe(new SimpleUseCaseListener<Result>() {
            @Override
            public void onActionRequired(List<Integer> codes) {

            }
        });
        useCase.execute();

        useCase = UseCase.fetch(RequestActionTestUseCase.class);
        useCase.execute(new PendingActionRequest.Builder()
                .input1("asdfas")
                .input2("qw98ewu9j")
                .build());

        assertTrue(useCase.isExecutionContinued());
    }

    @Test
    public void subscribe2Actors_onlyActor1ReceivesActionRequest() throws Exception {

        UseCase.fetch(RequestActionTestUseCase.class)
                .subscribe(RequestActionTestUseCase.ACTOR_1, new SimpleUseCaseListener<Result>() {
                    @Override
                    public void onActionRequired(List<Integer> codes) {
                        actor1Action++;
                    }
                })
                .subscribe(new SimpleUseCaseListener<Result>() {
                    @Override
                    public void onActionRequired(List<Integer> codes) {
                        actor2Action++;
                    }
                })
                .execute();

        assertEquals(1, actor1Action);
        assertEquals(0, actor2Action);
    }

    @Test
    public void subscribeNoActors_noCallbacks() throws Exception {

        UseCase.fetch(RequestActionTestUseCase.class)
                .execute();

        assertEquals(0, actor1Action);
        assertEquals(0, actor2Action);
    }
}
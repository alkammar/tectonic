package com.morkim.tectonic;

import android.support.annotation.NonNull;

import com.morkim.tectonic.entities.CancelCancelledPrerequisiteTestUseCase;
import com.morkim.tectonic.entities.CancelPendingPrerequisiteTestUseCase;
import com.morkim.tectonic.entities.CancelledPrerequisiteTestUseCase;
import com.morkim.tectonic.entities.CancelledTestUseCase;
import com.morkim.tectonic.entities.PendingActionRequest;
import com.morkim.tectonic.entities.PendingActionTestUseCase;
import com.morkim.tectonic.entities.TestResult;
import com.morkim.tectonic.entities.TestUseCase;
import com.morkim.tectonic.entities.UnfinishedTestUseCase;

import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import java.util.concurrent.Callable;

import io.reactivex.Scheduler;
import io.reactivex.android.plugins.RxAndroidPlugins;
import io.reactivex.functions.Function;
import io.reactivex.plugins.RxJavaPlugins;
import io.reactivex.schedulers.Schedulers;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;

public class CancelTest extends TecTonicTest {

    private long mainTimeStamp;
    private int onCancelMainCount;
    private int onStartCount;
    private int onCompleteCount;

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
    }

    @Before
    public void setup() {

        UseCase.unsubscribeAll();
        UseCase.clearAllInProgress();

        mainTimeStamp = 0;
        onCancelMainCount = 0;
    }

    @Test
    public void cancelMain_onCancelCalled() throws Exception {

        TestUseCase useCase;

        useCase = UseCase.fetch(CancelledTestUseCase.class);
        useCase.subscribe(new SimpleUseCaseListener<TestResult>() {
            @Override
            public void onComplete() {
                mainTimeStamp = System.nanoTime();
            }

            @Override
            public void onCancel() {
                onCancelMainCount++;
            }
        });
        useCase.execute();

        assertEquals(0, mainTimeStamp);
        assertEquals(1, onCancelMainCount);
    }

    @Test
    public void cancelMainTwice_onCancelCalledOnce() throws Exception {

        TestUseCase useCase;

        useCase = UseCase.fetch(CancelledTestUseCase.class);
        useCase.subscribe(new SimpleUseCaseListener<TestResult>() {
            @Override
            public void onComplete() {
                mainTimeStamp = System.nanoTime();
            }

            @Override
            public void onCancel() {
                onCancelMainCount++;
            }
        });
        useCase.execute();

        UseCase.cancel(CancelledTestUseCase.class);

        assertEquals(0, mainTimeStamp);
        assertEquals(1, onCancelMainCount);
    }

    @Test
    public void cancelPrerequisite_mainInProgress() throws Exception {

        CancelCancelledPrerequisiteTestUseCase useCase;

        useCase = UseCase.fetch(CancelCancelledPrerequisiteTestUseCase.class);
        useCase.subscribe(new SimpleUseCaseListener<TestResult>() {
            @Override
            public void onComplete() {
                mainTimeStamp = System.nanoTime();
            }
        });
        useCase.execute();

        assertNotEquals(0, useCase.getPrerequisiteTimeStamp());
        assertEquals(0, mainTimeStamp);
    }

    @Test
    public void cancelPrerequisiteExternally_mainInProgress() throws Exception {

        CancelPendingPrerequisiteTestUseCase useCase;

        useCase = UseCase.fetch(CancelPendingPrerequisiteTestUseCase.class);
        useCase.subscribe(new SimpleUseCaseListener<TestResult>() {
            @Override
            public void onComplete() {
                mainTimeStamp = System.nanoTime();
            }
        });
        useCase.execute();

        UseCase.cancel(PendingActionTestUseCase.class);

        assertNotEquals(0, useCase.getPrerequisiteTimeStamp());
        assertEquals(0, mainTimeStamp);
    }

    @Test
    public void cancelPrerequisiteTwice_onlyOneOnCancelCallback() throws Exception {

        CancelCancelledPrerequisiteTestUseCase useCase;

        useCase = UseCase.fetch(CancelCancelledPrerequisiteTestUseCase.class);
        useCase.subscribe(new SimpleUseCaseListener<TestResult>() {
            @Override
            public void onComplete() {
                mainTimeStamp = System.nanoTime();
            }
        });
        useCase.execute();

        UseCase.cancel(CancelledPrerequisiteTestUseCase.class);

        assertEquals(1, useCase.getOnCancelPrerequisiteCount());
    }

    @Test
    public void cancelCompleted_onCancelNotCalled() throws Exception {

        TestUseCase useCase;

        useCase = UseCase.fetch(TestUseCase.class);
        useCase.subscribe(new SimpleUseCaseListener<TestResult>() {
            @Override
            public void onComplete() {
                mainTimeStamp = System.nanoTime();
            }

            @Override
            public void onCancel() {
                onCancelMainCount++;
            }
        });
        useCase.execute();

        UseCase.cancel(TestUseCase.class);

        assertEquals(0, onCancelMainCount);
    }

    @Test
    public void executeCancelled_onStartCalled() throws Exception {

        UseCase.fetch(UnfinishedTestUseCase.class)
                .subscribe(new SimpleUseCaseListener<Result>() {

                    @Override
                    public void onStart() {
                        onStartCount++;
                    }
                }).execute();

        UseCase.cancel(UnfinishedTestUseCase.class);

        UseCase.fetch(UnfinishedTestUseCase.class)
                .execute();

        assertEquals(2, onStartCount);
    }

    @Test
    public void executeCancelledPrerequisite_mainContinueExecution() throws Exception {

        TestUseCase useCase;

        useCase = UseCase.fetch(CancelPendingPrerequisiteTestUseCase.class);
        useCase.subscribe(new SimpleUseCaseListener<TestResult>() {
            @Override
            public void onComplete() {
                mainTimeStamp = System.nanoTime();
            }
        });
        useCase.execute();

        UseCase.cancel(PendingActionTestUseCase.class);

        PendingActionTestUseCase prerequisiteUseCase = UseCase.fetch(PendingActionTestUseCase.class);
        prerequisiteUseCase.execute(new PendingActionRequest.Builder().build());

        assertNotEquals(0, mainTimeStamp);
    }

}
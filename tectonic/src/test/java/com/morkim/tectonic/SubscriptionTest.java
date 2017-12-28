package com.morkim.tectonic;

import android.support.annotation.NonNull;

import com.morkim.tectonic.entities.ErrorTestUseCase;
import com.morkim.tectonic.entities.PrerequisiteTestUseCase;
import com.morkim.tectonic.entities.TestResult;
import com.morkim.tectonic.entities.TestUseCase;

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
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class SubscriptionTest extends TecTonicTest {

    private boolean isSubscriptionExecuted;

    private int normalCount;
    private int disposableCount;

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

        isSubscriptionExecuted = false;

        UseCase.unsubscribeAll();
    }

    @Test
    public void subscribeThenExecute_SubscribedCallbacksExecuted() throws Exception {

        UseCase.subscribe(TestUseCase.class, new SimpleUseCaseListener<TestResult>() {
            @Override
            public void onUpdate(TestResult result) {
                isSubscriptionExecuted = true;
            }
        });

        TestUseCase useCase;

        useCase = UseCase.fetch(TestUseCase.class);
        useCase.execute();

        assertTrue(isSubscriptionExecuted);
    }

    @Test
    public void subscribeSameListenerInstance_onlyOneSubscription() throws Exception {

        SimpleUseCaseListener<TestResult> listener = new SimpleUseCaseListener<TestResult>() {
            @Override
            public void onUpdate(TestResult result) {
                normalCount++;
            }
        };

        UseCase.subscribe(TestUseCase.class, listener);
        UseCase.subscribe(TestUseCase.class, listener);

        TestUseCase useCase;

        useCase = UseCase.fetch(TestUseCase.class);
        useCase.execute();

        assertEquals(1, normalCount);
    }

    @Test
    public void unsubscribeThenExecute_NoCallbacksExecuted() throws Exception {

        SimpleUseCaseListener<TestResult> listener = new SimpleUseCaseListener<TestResult>() {
            @Override
            public void onUpdate(TestResult result) {
                isSubscriptionExecuted = true;
            }
        };

        UseCase.subscribe(TestUseCase.class, listener);

        UseCase.unsubscribe(TestUseCase.class, listener);

        TestUseCase useCase;

        useCase = UseCase.fetch(TestUseCase.class);
        useCase.execute();

        assertFalse(isSubscriptionExecuted);
    }

    @Test
    public void unsubscribeAllForUseCaseThenExecute_NoCallbacksExecuted() throws Exception {

        SimpleUseCaseListener<TestResult> listener = new SimpleUseCaseListener<TestResult>() {
            @Override
            public void onUpdate(TestResult result) {
                isSubscriptionExecuted = true;
            }
        };

        UseCase.subscribe(TestUseCase.class, listener);

        UseCase.unsubscribe(TestUseCase.class);

        TestUseCase useCase;

        useCase = UseCase.fetch(TestUseCase.class);
        useCase.execute();

        assertFalse(isSubscriptionExecuted);
    }

    @Test
    public void unsubscribeOtherUseCaseThenExecute_SubscribedCallbacksExecuted() throws Exception {

        SimpleUseCaseListener<TestResult> listener = new SimpleUseCaseListener<TestResult>() {
            @Override
            public void onUpdate(TestResult result) {
                isSubscriptionExecuted = true;
            }
        };

        UseCase.subscribe(TestUseCase.class, listener);

        UseCase.unsubscribe(PrerequisiteTestUseCase.class, listener);

        TestUseCase useCase;

        useCase = UseCase.fetch(TestUseCase.class);
        useCase.execute();

        assertTrue(isSubscriptionExecuted);
    }

    @Test
    public void subscribeDisposableAndComplete_DisposablesAReDisposed() throws Exception {

        UseCase.fetch(TestUseCase.class)
                .subscribe(new SimpleUseCaseListener<TestResult>() {
                    @Override
                    public void onComplete() {
                        normalCount++;
                    }
                })
                .subscribe(new SimpleDisposableUseCaseListener<TestResult>() {
                    @Override
                    public void onComplete() {
                        disposableCount++;
                    }
                })
                .subscribe(new SimpleDisposableUseCaseListener<TestResult>() {
                    @Override
                    public void onComplete() {
                        disposableCount++;
                    }
                })
                .subscribe(new SimpleUseCaseListener<TestResult>() {
                    @Override
                    public void onComplete() {
                        normalCount++;
                    }
                })
                .subscribe(new SimpleDisposableUseCaseListener<TestResult>() {
                    @Override
                    public void onComplete() {
                        disposableCount++;
                    }
                })
                .execute();

        UseCase.fetch(TestUseCase.class)
                .execute();

        assertEquals(4, normalCount);
        assertEquals(3, disposableCount);
    }

    @Test
    public void subscribeDisposableAndError_DisposablesAreDisposed() throws Exception {

        UseCase.fetch(ErrorTestUseCase.class)
                .subscribe(new SimpleDisposableUseCaseListener<TestResult>() {
                    @Override
                    public boolean onError(Throwable throwable) {
                        disposableCount++;
                        return false;
                    }
                })
                .subscribe(new SimpleUseCaseListener<TestResult>() {
                    @Override
                    public boolean onError(Throwable throwable) {
                        normalCount++;
                        return true;
                    }
                }).subscribe(new SimpleDisposableUseCaseListener<TestResult>() {
                    @Override
                    public boolean onError(Throwable throwable) {
                        disposableCount++;
                        return false;
                    }
                })
                .subscribe(new SimpleUseCaseListener<TestResult>() {
                    @Override
                    public boolean onError(Throwable throwable) {
                        normalCount++;
                        return false;
                    }
                })
                .subscribe(new SimpleDisposableUseCaseListener<TestResult>() {
                    @Override
                    public boolean onError(Throwable throwable) {
                        disposableCount++;
                        return false;
                    }
                })
                .subscribe(new SimpleUseCaseListener<TestResult>() {
                    @Override
                    public boolean onError(Throwable throwable) {
                        normalCount++;
                        return false;
                    }
                })
                .execute();

        UseCase.fetch(ErrorTestUseCase.class)
                .execute();

        assertEquals(6, normalCount);
        assertEquals(2, disposableCount);
    }

}
package com.morkim.tectonic;

import android.support.annotation.NonNull;

import com.morkim.tectonic.entities.CacheableTestUseCase;
import com.morkim.tectonic.entities.CacheableData;
import com.morkim.tectonic.entities.TestResult;
import com.morkim.tectonic.entities.TestUseCase;
import com.morkim.tectonic.entities.UserActor;

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

public class CacheTest extends TecTonicTest {

    private Result originalResult;
    private Result cachedResult;

    private int onStartCachedCalled;
    private int onCompleteCachedCalled;

    public int providedCount;
    public CacheableData data;

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

        originalResult = null;
        cachedResult = null;
        onStartCachedCalled = 0;
        onCompleteCachedCalled = 0;

        providedCount = 0;
    }

    @Test
    public void executeWithoutCaching_returnNewData() throws Exception {

        CacheableTestUseCase useCase;

        useCase = UseCase.fetch(CacheableTestUseCase.class);
        SimpleUseCaseListener<TestResult> originalResultListener = createOriginalResultListener();
        useCase.subscribe(originalResultListener);
        useCase.execute();

        useCase.unsubscribe(originalResultListener);

        useCase = UseCase.fetch(CacheableTestUseCase.class);
        useCase.subscribe(createCachedResultListener());
        useCase.execute();

        assertNotEquals(originalResult, cachedResult);
        assertEquals(1, onStartCachedCalled);
        assertEquals(1, onCompleteCachedCalled);
    }

    @Test
    public void executeNonCachable_returnNewData() throws Exception {

        TestUseCase useCase;

        useCase = UseCase.fetch(TestUseCase.class);
        SimpleUseCaseListener<TestResult> originalResultListener = createOriginalResultListener();
        useCase.subscribe(originalResultListener);
        useCase.execute();

        useCase.unsubscribe(originalResultListener);

        useCase = new TestUseCase();
        useCase.subscribe(createCachedResultListener());
        useCase.execute(UseCase.CACHED);

        assertNotEquals(originalResult, cachedResult);
        assertEquals(0, onStartCachedCalled);
        assertEquals(0, onCompleteCachedCalled);
    }

    @Test
    public void executeSecondTime_returnCachedData() throws Exception {

        CacheableTestUseCase useCase;

        useCase = UseCase.fetch(CacheableTestUseCase.class);
        useCase.subscribe(createOriginalResultListener());
        useCase.execute();

        useCase = UseCase.fetch(CacheableTestUseCase.class);
        useCase.subscribe(createCachedResultListener());
        useCase.execute(UseCase.CACHED);

        assertEquals(originalResult, cachedResult);
        assertEquals(1, onStartCachedCalled);
        assertEquals(1, onCompleteCachedCalled);
    }

    @Test
    public void executeAfterClear_returnNewData() throws Exception {

        CacheableTestUseCase useCase;

        useCase = UseCase.fetch(CacheableTestUseCase.class);
        SimpleUseCaseListener<TestResult> originalResultListener = createOriginalResultListener();
        useCase.subscribe(originalResultListener);
        useCase.execute();

        UseCase.clearCache(CacheableTestUseCase.class);
        useCase.unsubscribe(originalResultListener);

        useCase = UseCase.fetch(CacheableTestUseCase.class);
        useCase.subscribe(createCachedResultListener());
        useCase.execute(UseCase.CACHED);

        assertNotEquals(originalResult, cachedResult);
        assertEquals(1, onStartCachedCalled);
        assertEquals(1, onCompleteCachedCalled);
    }

    @Test
    public void cacheStep_restart_usesCache() {

        final CacheableTestUseCase useCase = UseCase.fetch(CacheableTestUseCase.class);
        UserActor userActor = new UserActor() {

            @Override
            public CacheableData askToProvideData() {
                providedCount++;
                return new CacheableData();
            }

            @Override
            public void doYourThing() {
                useCase.restart();
            }
        };
        useCase.setUserActor(userActor);
        useCase.execute();

        assertEquals(1, providedCount);
    }

    @NonNull
    private SimpleUseCaseListener<TestResult> createOriginalResultListener() {
        return new SimpleUseCaseListener<TestResult>() {
            @Override
            public void onUpdate(TestResult result) {
                originalResult = result;
            }
        };
    }

    @NonNull
    private SimpleUseCaseListener<TestResult> createCachedResultListener() {
        return new SimpleUseCaseListener<TestResult>() {

            @Override
            public void onStart() {
                onStartCachedCalled++;
            }

            @Override
            public void onUpdate(TestResult result) {
                cachedResult = result;
            }

            @Override
            public void onComplete() {
                onCompleteCachedCalled++;
            }
        };
    }
}
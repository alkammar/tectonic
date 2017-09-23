package com.morkim.tectonic;

import android.support.annotation.NonNull;

import com.morkim.tectonic.entities.PendingActionRequest;
import com.morkim.tectonic.entities.TestResult;
import com.morkim.tectonic.entities.TestUseCase;

import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class CallbacksTest {

    private int isOnStartCalledCount;
    private int isOnUpdateCalledCount;
    private int isOnCompleteCalledCount;
    private boolean isOnExecuteCalled;
    private boolean isOnPostExecuteCalled;

    @Before
    public void setup() {

        isOnStartCalledCount = 0;
        isOnUpdateCalledCount = 0;
        isOnCompleteCalledCount = 0;
        isOnExecuteCalled = false;
        isOnPostExecuteCalled = false;
    }

    @Test
    public void executeNoListener_callbacksNotCalled() throws Exception {

        UseCase useCase = new MainTestUseCase();

        useCase.execute();

        assertEquals(0, isOnStartCalledCount);
        assertEquals(0, isOnUpdateCalledCount);
        assertEquals(0, isOnCompleteCalledCount);
    }

    @Test
    public void execute_callbacksCalled() throws Exception {

        TestUseCase useCase = new TestUseCase();
        useCase.subscribe(createOnStartCounterListener())
                .subscribe(createOnUpdateCounterListener())
                .subscribe(createOnCompleteCounterListener());

        useCase.execute();

        assertEquals(1, isOnStartCalledCount);
        assertEquals(1, isOnUpdateCalledCount);
        assertEquals(1, isOnCompleteCalledCount);
    }

    @Test
    public void executeMultipleSubscription_callbacksCalled() throws Exception {

        TestUseCase useCase = new TestUseCase();
        useCase.subscribe(createOnStartCounterListener())
                .subscribe(createOnUpdateCounterListener())
                .subscribe(createOnCompleteCounterListener());
        UseCase.subscribe(TestUseCase.class, createOnStartCounterListener());
        UseCase.subscribe(TestUseCase.class, createOnUpdateCounterListener());
        UseCase.subscribe(TestUseCase.class, createOnCompleteCounterListener());
        UseCase.subscribe(TestUseCase.class, createOnStartCounterListener());
        UseCase.subscribe(TestUseCase.class, createOnUpdateCounterListener());
        UseCase.subscribe(TestUseCase.class, createOnCompleteCounterListener());

        useCase.execute();

        assertEquals(3, isOnStartCalledCount);
        assertEquals(3, isOnUpdateCalledCount);
        assertEquals(3, isOnCompleteCalledCount);
    }

    @Test
    public void executeMultipleSubscriptionWithUnsubscribe_callbacksCalled() throws Exception {

        TestUseCase useCase = new TestUseCase();

        useCase.subscribe(createOnStartCounterListener());
        useCase.subscribe(createOnUpdateCounterListener());
        useCase.subscribe(createOnCompleteCounterListener());

        UseCase.OnStartListener onStartCounterListener = createOnStartCounterListener();
        UseCase.OnUpdateListener onUpdateCounterListener = createOnUpdateCounterListener();
        UseCase.OnCompleteListener onCompleteCounterListener = createOnCompleteCounterListener();

        UseCase.subscribe(TestUseCase.class, onStartCounterListener);
        UseCase.subscribe(TestUseCase.class, onUpdateCounterListener);
        UseCase.subscribe(TestUseCase.class, onCompleteCounterListener);
        UseCase.unsubscribe(TestUseCase.class, onStartCounterListener);
        UseCase.unsubscribe(TestUseCase.class, onUpdateCounterListener);
        UseCase.unsubscribe(TestUseCase.class, onCompleteCounterListener);

        UseCase.subscribe(TestUseCase.class, createOnStartCounterListener());
        UseCase.subscribe(TestUseCase.class, createOnUpdateCounterListener());
        UseCase.subscribe(TestUseCase.class, createOnCompleteCounterListener());

        useCase.execute();

        assertEquals(2, isOnStartCalledCount);
        assertEquals(2, isOnUpdateCalledCount);
        assertEquals(2, isOnCompleteCalledCount);
    }

    @Test
    public void executeAllUnsubscribed_callbacksNotCalled() throws Exception {

        TestUseCase useCase = new TestUseCase();
        UseCase.OnStartListener callbackCounterListener1 = createOnStartCounterListener();
        useCase.subscribe(callbackCounterListener1);
        UseCase.OnStartListener callbackCounterListener2 = createOnStartCounterListener();
        useCase.subscribe(callbackCounterListener2);
        UseCase.OnStartListener callbackCounterListener3 = createOnStartCounterListener();
        useCase.subscribe(callbackCounterListener3);

        useCase.unsubscribe();
        useCase.unsubscribe();
        useCase.unsubscribe();

        useCase.execute();

        assertEquals(0, isOnStartCalledCount);
        assertEquals(0, isOnUpdateCalledCount);
        assertEquals(0, isOnCompleteCalledCount);
    }

    @Test
    public void executeUnsubscribeNonSubscriber_correctCountCalled() throws Exception {

        TestUseCase useCase = new TestUseCase();

        useCase.subscribe(createOnStartCounterListener());
        useCase.subscribe(createOnUpdateCounterListener());
        useCase.subscribe(createOnCompleteCounterListener());

        UseCase.subscribe(TestUseCase.class, createOnStartCounterListener());
        UseCase.subscribe(TestUseCase.class, createOnUpdateCounterListener());
        UseCase.subscribe(TestUseCase.class, createOnCompleteCounterListener());

        UseCase.subscribe(TestUseCase.class, createOnStartCounterListener());
        UseCase.subscribe(TestUseCase.class, createOnUpdateCounterListener());
        UseCase.subscribe(TestUseCase.class, createOnCompleteCounterListener());

        UseCase.OnStartListener OnStartCounterListener = createOnStartCounterListener();
        UseCase.OnUpdateListener OnUpdateCounterListener = createOnUpdateCounterListener();
        UseCase.OnCompleteListener OnCompleteCounterListener = createOnCompleteCounterListener();

        UseCase.unsubscribe(TestUseCase.class, OnStartCounterListener);
        UseCase.unsubscribe(TestUseCase.class, OnUpdateCounterListener);
        UseCase.unsubscribe(TestUseCase.class, OnCompleteCounterListener);

        useCase.execute();

        assertEquals(3, isOnStartCalledCount);
        assertEquals(3, isOnUpdateCalledCount);
        assertEquals(3, isOnCompleteCalledCount);
    }

    @Test
    public void executeWithFinish_onCompleteCalled() throws Exception {

        TestUseCase useCase = new TestUseCase();
        useCase.subscribe(createOnCompleteCounterListener());

        useCase.execute();

        assertEquals(1, isOnCompleteCalledCount);
    }

    @Test
    public void executeWithoutFinish_onCompleteNotCalled() throws Exception {

        TestUseCase useCase = new TestUseCase() {
            @Override
            protected void onExecute(Request request) {

                TestResult result = new TestResult();

                updateSubscribers(result);
            }
        };
        useCase.subscribe(createOnStartCounterListener());

        useCase.execute();

        assertEquals(0, isOnCompleteCalledCount);
    }

    @Test
    public void executeNoSubscribers_onExecuteCalled() throws Exception {

        TestUseCase useCase = new TestUseCase() {

            @Override
            protected void onExecute(Request request) {

                isOnExecuteCalled = true;
            }
        };

        useCase.execute();

        assertTrue(isOnExecuteCalled);
    }

    @Test
    public void executeNoSubscribers_onPostExecuteCalledAfterComplete() throws Exception {

        MainTestUseCase useCase = new MainTestUseCase() {

            @Override
            protected void onPostExecute() {

                isOnPostExecuteCalled = true;
            }
        };

        useCase.execute();

        assertTrue(isOnPostExecuteCalled);
    }

    @NonNull
    private UseCase.OnStartListener createOnStartCounterListener() {
        return new UseCase.OnStartListener() {

            @Override
            public void onStart() {
                isOnStartCalledCount++;
            }
        };
    }

    @NonNull
    private UseCase.OnUpdateListener<TestResult> createOnUpdateCounterListener() {
        return new UseCase.OnUpdateListener<TestResult>() {

            @Override
            public void onUpdate(TestResult result) {
                isOnUpdateCalledCount++;
            }
        };
    }

    @NonNull
    private UseCase.OnCompleteListener createOnCompleteCounterListener() {
        return new UseCase.OnCompleteListener() {

            @Override
            public void onComplete() {
                isOnCompleteCalledCount++;
            }
        };
    }

    private class MainTestUseCase extends UseCase<PendingActionRequest, TestResult> {

        @Override
        protected void onExecute(PendingActionRequest request) {


            TestResult result = new TestResult();

            updateSubscribers(result);

            finish();
        }
    }

}
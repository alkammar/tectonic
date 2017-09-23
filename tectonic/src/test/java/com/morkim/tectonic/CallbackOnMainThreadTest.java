//package com.morkim.tectonic;
//
//import android.os.SystemClock;
//
//import com.morkim.tectonic.entities.PendingActionRequest;
//import com.morkim.tectonic.entities.PendingActionTestUseCase;
//import com.morkim.tectonic.entities.TestResult;
//
//import org.junit.Before;
//import org.junit.Test;
//
//import static org.junit.Assert.assertTrue;
//
//public class CallbackOnMainThreadTest {
//
//    private boolean executionContinued;
//
//    @Before
//    public void setup() {
//
//        UseCase.unsubscribeAll();
//        UseCase.clearAllInProgress();
//
//        executionContinued = false;
//    }
//
//    @Test
//    public void executeMultiple_onlyNewSubscriptionOnStartAndOnExecute() throws Exception {
//
//        MainTestUseCase useCase = new MainTestUseCase();
//        useCase.execute();
//
//        useCase = new MainTestUseCase();
//        useCase.execute(new PendingActionRequest.Builder().build());
//
//        SystemClock.sleep(1000);
//
//        assertTrue(executionContinued);
//    }
//
//    private class MainTestUseCase extends PendingActionTestUseCase {
//
//        @Override
//        protected void onExecute(PendingActionRequest request) {
//
//            new Thread(new Runnable() {
//                @Override
//                public void run() {
//
//                    executionContinued = true;
//
//                    TestResult result = new TestResult();
//
//                    updateSubscribers(result);
//
//                    finish();
//                }
//            }).start();
//        }
//    }
//}
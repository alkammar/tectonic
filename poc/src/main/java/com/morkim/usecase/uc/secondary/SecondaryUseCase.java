//package com.morkim.usecase.uc.secondary;
//
//import android.os.SystemClock;
//
//import com.morkim.tectonic.usecase.PrimaryActor;
//import com.morkim.tectonic.usecase.UseCase;
//import com.morkim.usecase.app.AppTrigger;
//import com.morkim.usecase.di.AppInjector;
//import com.morkim.usecase.uc.main.ExpiredCredentials;
//
//import javax.inject.Inject;
//
//
//public class SecondaryUseCase extends UseCase<AppTrigger.Event, String> {
//
//    private static final int STEP = 1;
//
//    @Inject
//    Backend backend;
//
//    @Inject
//    Authenticator authenticator;
//
//    @Inject
//    UI ui;
//
//    @Override
//    protected void onCreate() {
//        super.onCreate();
//
//        AppInjector.getSecondaryUseCaseComponent().inject(this);
//    }
//
//    @Override
//    protected void onExecute() throws InterruptedException {
//
//        try {
//            String someData = backend.retrieveSomeData();
//
//            for (int i = 0; i < 100 / STEP; i++) {
//                SystemClock.sleep(50);
//
//                ui.updateResult("" + (i + 1) * STEP);
//            }
//
//            complete("Final result sent by the main use case\n" + someData);
//
//        } catch (ExpiredCredentials e) {
//            authenticator.refreshAuthentication();
//            restart();
//        }
//    }
//
//    public interface Backend {
//
//        String retrieveSomeData() throws ExpiredCredentials;
//    }
//
//    public interface Authenticator {
//
//        void refreshAuthentication() throws InterruptedException;
//    }
//
//    public interface UI extends PrimaryActor<AppTrigger.Event, String> {
//
//        void updateResult(String data);
//    }
//}

package com.morkim.usecase.uc.secondary;

import com.morkim.tectonic.usecase.PrimaryActor;
import com.morkim.tectonic.usecase.UseCase;
import com.morkim.usecase.app.AppTrigger;
import com.morkim.usecase.di.AppInjector;
import com.morkim.usecase.model.SecondaryModel;
import com.morkim.usecase.uc.main.ExpiredCredentials;

import java.util.Set;

import javax.inject.Inject;


public class SecondaryUseCase extends UseCase<AppTrigger.Event, SecondaryModel> {

    @Inject
    Backend backend;

    @Inject
    Authenticator authenticator;

    @Inject
    UI ui;

    @Override
    protected void onCreate() {
        super.onCreate();

        AppInjector.getSecondaryUseCaseComponent().inject(this);
    }

    @Override
    protected void onAddPreconditions(Set<AppTrigger.Event> events) {
        super.onAddPreconditions(events);

//        events.add(AppTrigger.Event.PRE_CONDITION_MAIN);
    }

    @Override
    protected void onExecute() throws InterruptedException {

        try {

            String data1 = ui.askForData1();
            String data2 = ui.askForData2();
            double data3 = ui.askForData3();

            validateData3(data3);

            ui.askToConfirm();

            ui.block();
            SecondaryModel result = backend.requestSomething(data1, data2, data3);
            ui.unblock();

            complete(result);

        } catch (NoAmountException e) {
            ui.showError(e);
            restart();
        } catch (ExpiredCredentials e) {
            authenticator.refreshAuthentication();
            restart();
        } catch (SpecificBackendError | GeneralBackendError e) {
            ui.showError(e);
        }
    }

    private void validateData3(double data3) throws NoAmountException {
        if (data3 == 0) throw new NoAmountException();
    }

    public interface UI extends PrimaryActor<AppTrigger.Event, SecondaryModel> {

        String askForData1() throws InterruptedException;

        String askForData2() throws InterruptedException;

        Double askForData3() throws InterruptedException;

        void askToConfirm() throws InterruptedException;

        void showError(Exception e);

        void block();

        void unblock();
    }

    public interface Backend {

        SecondaryModel requestSomething(String data1, String data2, double data3)
                throws ExpiredCredentials, GeneralBackendError, SpecificBackendError;
    }

    public interface Authenticator {

        void refreshAuthentication() throws InterruptedException;
    }
}

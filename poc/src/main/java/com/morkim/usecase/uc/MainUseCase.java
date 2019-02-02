package com.morkim.usecase.uc;

import com.morkim.tectonic.usecase.PrimaryActor;
import com.morkim.tectonic.usecase.SecondaryActor;
import com.morkim.tectonic.usecase.UndoException;
import com.morkim.tectonic.usecase.UseCase;
import com.morkim.usecase.di.AppInjector;

import java.util.Set;

import javax.inject.Inject;

import lib.morkim.uc.ExpiredCredentials;


public class MainUseCase extends UseCase<String> {

    private static final int STEP = 1;

    @Inject
    Backend backend;

    @Inject
    Authenticator authenticator;

    @Inject
    UI ui;

    @Override
    protected void onCreate() {
        super.onCreate();

        AppInjector.getMainUseCaseComponent().inject(this);
    }

    @Override
    protected void onAddPrimaryActors(Set<PrimaryActor> actors) {
        actors.add(ui);
    }

    @Override
    protected void onAddSecondaryActors(Set<SecondaryActor> actors) {
        actors.add(authenticator);
        actors.add(backend);
    }

    @Override
    protected void onExecute() throws InterruptedException, UndoException {

        try {
            String someData = backend.retrieveSomeData();

            for (int i = 0; i < 100 / STEP; i++) {
                Thread.sleep(50);

                ui.updateResult("" + (i + 1) * STEP);
            }

            complete("Final result sent by the main use case\n" + someData);

        } catch (ExpiredCredentials e) {
            authenticator.refreshAuthentication();
            retry();
        }
    }

    public interface Backend<E> extends SecondaryActor<E> {

        String retrieveSomeData() throws ExpiredCredentials;
    }

    public interface Authenticator<E> extends SecondaryActor<E> {

        void refreshAuthentication() throws InterruptedException, UndoException;
    }

    public interface UI<E> extends PrimaryActor<E> {

        void updateResult(String data);
    }
}

package com.morkim.usecase.uc;

import com.morkim.tectonic.usecase.PrimaryActor;
import com.morkim.tectonic.usecase.SecondaryActor;
import com.morkim.tectonic.usecase.TectonicEvent;
import com.morkim.tectonic.usecase.UseCase;
import com.morkim.usecase.di.AppInjector;

import java.util.Set;

import javax.inject.Inject;


public class LogoutUser extends UseCase<Void> {

    @Inject
    Backend backend;

    @Inject
    UI ui;

    @Override
    protected void onCreate() {
        super.onCreate();

        AppInjector.getLogoutUserComponent().inject(this);
    }

    @Override
    protected void onAddPrimaryActors(Set<PrimaryActor> actors) {
        actors.add(ui);
    }

    @Override
    protected void onAddSecondaryActors(Set<SecondaryActor> actors) {
        actors.add(backend);
    }

    @Override
    protected void onExecute() throws InterruptedException {

        backend.logout();
        ui.showLogin();

        complete();
    }

    public interface Backend<E extends TectonicEvent> extends SecondaryActor<E, Void> {

        boolean logout();
    }

    public interface UI<E extends TectonicEvent> extends PrimaryActor<E, Void> {

        void showLogin();
    }
}

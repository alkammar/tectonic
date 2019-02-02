package com.morkim.usecase.uc;

import com.morkim.tectonic.usecase.PrimaryActor;
import com.morkim.tectonic.usecase.SecondaryActor;
import com.morkim.tectonic.usecase.UndoException;
import com.morkim.tectonic.usecase.UnexpectedStep;
import com.morkim.tectonic.usecase.UseCase;
import com.morkim.usecase.di.AppInjector;

import java.util.Set;

import javax.inject.Inject;


public class LoginUser extends UseCase<Void> {

    @Inject
    Backend backend;

    @Inject
    UI ui;

    @Override
    protected void onCreate() {
        super.onCreate();

        AppInjector.getLoginUserComponent().inject(this);
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
    protected void onExecute() throws InterruptedException, UndoException {

        try {
            String password = ui.askForPassword();

            backend.validateCredentials(password);
            complete();

        } catch (InvalidLogin e) {
            ui.show(e);
            retry();
        } catch (UnexpectedStep e) {
            if (e.getCause() instanceof UserWantsToRegister) {
                backend.register();
                complete();
            }
        }
    }

    public interface Backend<E> extends SecondaryActor<E> {

        void validateCredentials(String password) throws InvalidLogin;

        void register() throws InterruptedException, UndoException;
    }

    public interface UI<E> extends PrimaryActor<E> {

        String askForPassword() throws InterruptedException, UnexpectedStep;

        void show(Exception e);
    }

}

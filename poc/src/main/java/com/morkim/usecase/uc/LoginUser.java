package com.morkim.usecase.uc;

import com.morkim.tectonic.usecase.PrimaryActor;
import com.morkim.tectonic.usecase.UseCase;
import com.morkim.usecase.app.UseCaseExecutor;
import com.morkim.tectonic.usecase.UnexpectedStep;
import com.morkim.usecase.di.AppInjector;

import java.util.Set;

import javax.inject.Inject;


public class LoginUser extends UseCase<UseCaseExecutor.Event, Void> {

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
    protected void onAddPreconditions(Set<UseCaseExecutor.Event> events) {

    }

    @Override
    protected void onExecute() throws InterruptedException {

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

    public interface Backend {

        void validateCredentials(String password) throws InvalidLogin;

        void register() throws InterruptedException;
    }

    public interface UI extends PrimaryActor<UseCaseExecutor.Event, Void> {

        String askForPassword() throws InterruptedException, UnexpectedStep;

        void show(Exception e);
    }

}

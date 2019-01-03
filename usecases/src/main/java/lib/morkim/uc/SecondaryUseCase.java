package lib.morkim.uc;

import com.morkim.tectonic.usecase.PrimaryActor;
import com.morkim.tectonic.usecase.SecondaryActor;
import com.morkim.tectonic.usecase.TectonicEvent;
import com.morkim.tectonic.usecase.UndoException;
import com.morkim.tectonic.usecase.UseCase;

import java.util.Set;

import javax.inject.Inject;

import lib.morkim.di.UseCaseInjector;
import lib.morkim.model.SecondaryModel;


public class SecondaryUseCase extends UseCase<SecondaryModel> {

    @Inject
    Backend backend;

    @Inject
    Authenticator authenticator;

    @Inject
    UI ui;

    @Override
    protected void onCreate() {
        super.onCreate();

        UseCaseInjector.getSecondaryUseCaseComponent().inject(this);
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

            String data1 = ui.askForData1();
            String data2 = ui.askForData2();
            double data3 = ui.askForData3();

            validateData3(data3);

            ui.askToConfirm();

            ui.block();
            SecondaryModel result = backend.requestSomething(data1, data2, data3);
            ui.unblock();

            complete(result);

        } catch (InvalidValueException e) {
            ui.showError(e);
            retry();
        } catch (ExpiredCredentials e) {
            authenticator.refreshAuthentication();
            retry();
        } catch (SpecificBackendError | GeneralBackendError e) {
            ui.showError(e);
        }
    }

    private void validateData3(double data3) throws InvalidValueException {
        if (data3 == 0) throw new InvalidValueException();
    }

    public interface UI<E extends TectonicEvent> extends PrimaryActor<E, SecondaryModel> {

        String askForData1() throws InterruptedException, UndoException;

        String askForData2() throws InterruptedException, UndoException;

        Double askForData3() throws InterruptedException, UndoException;

        void askToConfirm() throws InterruptedException;

        void showError(Exception e);

        void block();

        void unblock();
    }

    public interface Backend<E> extends SecondaryActor<E, Void> {

        SecondaryModel requestSomething(String data1, String data2, double data3)
                throws ExpiredCredentials, GeneralBackendError, SpecificBackendError;
    }

    public interface Authenticator<E> extends SecondaryActor<E, Void> {

        void refreshAuthentication() throws InterruptedException, UndoException;
    }
}

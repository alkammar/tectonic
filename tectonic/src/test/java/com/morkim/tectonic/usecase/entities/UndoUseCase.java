package com.morkim.tectonic.usecase.entities;

import com.morkim.tectonic.usecase.PrimaryActor;
import com.morkim.tectonic.usecase.Random;
import com.morkim.tectonic.usecase.SecondaryActor;
import com.morkim.tectonic.usecase.UndoException;

import java.util.Set;
import java.util.concurrent.ExecutionException;

public class UndoUseCase extends SimpleUseCase {

    private PActor primaryActor;
    private SActor secondaryActor;

    @Override
    protected void onAddPrimaryActors(Set<PrimaryActor> actors) {
        super.onAddPrimaryActors(actors);

        actors.add(primaryActor);
    }

    @Override
    protected void onAddSecondaryActors(Set<SecondaryActor> actors) {
        super.onAddSecondaryActors(actors);

        actors.add(secondaryActor);
    }

    @Override
    protected void onExecute() throws InterruptedException, UndoException {
        super.onExecute();

        try {
            Random<StepData> data1 = primaryActor.requestData1();

            Random<StepData> data2 = primaryActor.requestData2();

            primaryActor.requestConfirmation();

            data1.value().access();
            data2.value().access();

            StepData data3 = secondaryActor.requestData3();
            data3.access();

            Random<StepData> data4 = primaryActor.requestData4();

            Random<StepData> data5 = primaryActor.requestData5();

            primaryActor.requestAnotherConfirmation();

            data4.value().access();
            data5.value().access();

            Random<StepData> data7 = primaryActor.requestData7();

            Random<StepData> data8 = primaryActor.requestData8();

            primaryActor.requestYetAnotherConfirmation();

            StepData data9 = secondaryActor.requestData9();
            data9.access();
        } catch (ExecutionException e) {

        }

        complete();
    }

    public void setPrimaryActor(PActor primaryActor) {
        this.primaryActor = primaryActor;
    }

    public void setSecondaryActor(SActor secondaryActor) {
        this.secondaryActor = secondaryActor;
    }

    public interface PActor extends PrimaryActor<Integer, Void> {

        Random<StepData> requestData1() throws InterruptedException, UndoException;

        Random<StepData> requestData2() throws InterruptedException, UndoException;

        void requestConfirmation() throws InterruptedException, UndoException;

        Random<StepData> requestData4() throws InterruptedException, UndoException;

        Random<StepData> requestData5() throws InterruptedException, UndoException;

        void requestAnotherConfirmation() throws InterruptedException, UndoException;

        Random<StepData> requestData7() throws InterruptedException, UndoException;

        Random<StepData> requestData8() throws InterruptedException, UndoException;

        void requestYetAnotherConfirmation() throws InterruptedException, UndoException;
    }

    public interface SActor extends SecondaryActor<Integer, Void> {

        StepData requestData3() throws InterruptedException, UndoException, ExecutionException;

        StepData requestData9() throws UndoException, InterruptedException, ExecutionException;
    }
}

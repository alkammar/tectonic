package com.morkim.tectonic.usecase.entities;

import com.morkim.tectonic.usecase.PreconditionActor;
import com.morkim.tectonic.usecase.PrimaryActor;
import com.morkim.tectonic.usecase.TectonicEvent;
import com.morkim.tectonic.usecase.UndoException;
import com.morkim.tectonic.usecase.UseCase;

import java.util.UUID;
import java.util.concurrent.ExecutionException;

public class ContainerUseCase extends SimpleUseCase {

    private Actor actor;
    private Class<? extends UseCase<Void>> subUseCase;

    public void addPrimaryActor(Actor primaryActor) {
        super.addPrimaryActor(primaryActor);

        this.actor = primaryActor;
    }

    @Override
    protected void onExecute() throws InterruptedException, UndoException {
        super.onExecute();

        try {
            actor.doBeforeSubUseCase();

            execute(subUseCase);

            actor.doAfterSubUseCase();

            complete();

        } catch (ExecutionException e) {
            e.printStackTrace();
        }
    }

    public void setSubUseCase(Class<? extends UseCase<Void>> subUseCase) {
        this.subUseCase = subUseCase;
    }

    public interface Actor extends PrimaryActor<TectonicEvent>, PreconditionActor<TectonicEvent> {

        void doBeforeSubUseCase() throws InterruptedException, ExecutionException, UndoException;

        void doAfterSubUseCase() throws InterruptedException, UndoException, ExecutionException;

    }

}

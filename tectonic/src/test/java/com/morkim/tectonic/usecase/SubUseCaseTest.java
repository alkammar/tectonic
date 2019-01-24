package com.morkim.tectonic.usecase;

import com.morkim.tectonic.flow.Step;
import com.morkim.tectonic.usecase.entities.CompletedUseCase;
import com.morkim.tectonic.usecase.entities.ContainerUseCase;
import com.morkim.tectonic.usecase.entities.FailingPreconditionsUseCase;
import com.morkim.tectonic.usecase.entities.InterruptableUseCase;
import com.morkim.tectonic.usecase.entities.SimpleTriggers;
import com.morkim.tectonic.usecase.entities.SimpleUseCase;

import org.junit.Before;
import org.junit.Test;

import java.util.UUID;
import java.util.concurrent.ExecutionException;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class SubUseCaseTest extends ConcurrentTectonicTest {


    private static final UUID PK1 = UUID.randomUUID();

    private volatile boolean onUndoCalled;

    @Before
    public void setup() {
        super.setup();

        onUndoCalled = false;
    }

    @Test
    public void abort_sub_use_case__undo_thrown_in_main_use_case() throws InterruptedException {

        ContainerUseCase useCase = UseCase.fetch(ContainerUseCase.class);
        useCase.setExecutor(new SimpleTriggers() {
            @Override
            public TectonicEvent trigger(final TectonicEvent event, PreconditionActor preconditionActor, PrimaryActor primaryActor, final ResultActor resultActor, TectonicEvent contextEvent) {

                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            Thread.sleep(200);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                        resultActor.onAbort(null);
                    }
                }).start();

                return event;
            }
        });
        useCase.addPrimaryActor(new ContainerUseCase.Actor() {

            @Override
            public void doSomething() throws InterruptedException, ExecutionException, UndoException {
                useCaseHandle.waitFor(this, PK1);
            }

            @Override
            public void onStart(TectonicEvent event, UseCaseHandle handle) {
                useCaseHandle = handle;
            }

            @Override
            public void onUndo(Step step, boolean inclusive) {
                onUndoCalled = true;
                useCaseHandle.abort();
            }

            @Override
            public void onComplete(TectonicEvent event) {

            }

            @Override
            public void onAbort(TectonicEvent event) {

            }
        });
        useCase.execute();

        sleep();

        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                sleep();
                useCaseHandle.replyWith(PK1);
            }
        });
        thread.start();

        thread.join();

        useCaseThread.join();

        assertTrue(onUndoCalled);
    }
}
package com.morkim.tectonic.usecase;

import com.morkim.tectonic.flow.Step;
import com.morkim.tectonic.usecase.entities.AbortedUseCase;
import com.morkim.tectonic.usecase.entities.CompletedUseCase;
import com.morkim.tectonic.usecase.entities.ContainerUseCase;
import com.morkim.tectonic.usecase.entities.SimpleTriggers;

import org.junit.Before;
import org.junit.Test;

import java.util.UUID;
import java.util.concurrent.ExecutionException;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertTrue;

public class SubUseCaseTest extends ConcurrentTectonicTest {


    private static final UUID CK1 = UUID.randomUUID();
    private static final UUID CK2 = UUID.randomUUID();

    private static final UUID SK1 = UUID.randomUUID();

    private volatile boolean onUndoCalled;
    private volatile boolean onSubCompleteCalled;
    private volatile boolean onSubAbortedCalled;
    private volatile boolean onActorCompleteCalled;
    private volatile boolean onSubObserverCompleteCalled;
    private volatile boolean onSubObserverAbortedCalled;
    private long containerCompleteTimestamp;
    private long subCompleteTimestamp;
    private long subOnStartCount;
    private long subOnCompleteCount;
    private long containerResultOnCompleteCount;
    private int doBeforeSubUseCaseCalled;
    private int subDoSomethingCalled;

    private ContainerActor containerActor = new ContainerActor();
    private SubCompletedActor subCompletedActor = new SubCompletedActor();
    private ThreadManagerImpl subThreadManager;
    private boolean onSubUndoCalled;

    private UseCaseHandle subHandle;

    @Before
    public void setup() {
        super.setup();

        onUndoCalled = false;
        onActorCompleteCalled = false;
        onSubCompleteCalled = false;
        onSubAbortedCalled = false;
        onSubObserverCompleteCalled = false;
        onSubObserverAbortedCalled = false;
        containerCompleteTimestamp = 0;
        subCompleteTimestamp = 0;
        subOnStartCount = 0;
        subOnCompleteCount = 0;
        containerResultOnCompleteCount = 0;
        doBeforeSubUseCaseCalled = 0;
        subDoSomethingCalled = 0;
    }

    @Test
    public void abort_sub_use_case__undo_thrown_in_main_use_case() throws InterruptedException {

        ContainerUseCase container = UseCase.fetch(ContainerUseCase.class);
        container.setExecutor(new TestExecutor());
        AbortedUseCase sub = UseCase.fetch(AbortedUseCase.class);
        sub.addPrimaryActor(subCompletedActor);
        subThreadManager = new ThreadManagerImpl();
        sub.setThreadManager(subThreadManager);
        container.setSubUseCase(AbortedUseCase.class);
        container.addPrimaryActor(containerActor);
        container.execute();

        sleep();

        reply1();

        sleep();
        subThreadManager.thread.join();

        sub = UseCase.fetch(AbortedUseCase.class);
        sub.addPrimaryActor(subCompletedActor);
        subThreadManager = new ThreadManagerImpl();
        sub.setThreadManager(subThreadManager);

        reply1();

        sleep();
        subThreadManager.thread.join();

        abort();

        waitForUseCaseToFinish();

        assertTrue(onUndoCalled);
        assertFalse(onSubCompleteCalled);
        assertTrue(onSubObserverAbortedCalled);
        assertEquals(3, doBeforeSubUseCaseCalled);
        assertEquals(2, subOnStartCount);
    }

    @Test
    public void complete_container__sub_use_case_completed_after_container_use_case() throws InterruptedException {

        final ContainerUseCase container = UseCase.fetch(ContainerUseCase.class);
        container.setExecutor(new TestExecutor());
        CompletedUseCase sub = UseCase.fetch(CompletedUseCase.class);
        sub.addPrimaryActor(subCompletedActor);
        subThreadManager = new ThreadManagerImpl();
        sub.setThreadManager(subThreadManager);
        container.setSubUseCase(CompletedUseCase.class);
        container.addPrimaryActor(containerActor);
        containerActor.complete = true;
        container.execute();

        sleep();

        reply1();
        replySub();
        reply2();

        waitForUseCaseToFinish();

        subThreadManager.thread.join();

        assertTrue(onActorCompleteCalled);
        assertTrue(onSubCompleteCalled);
        assertTrue(onSubObserverCompleteCalled);
        assertEquals(1, subOnCompleteCount);
        assertEquals(0, containerResultOnCompleteCount);
        assertNotEquals(0, subCompleteTimestamp);
        assertNotEquals(0, containerCompleteTimestamp);
        assertTrue(subCompleteTimestamp >= containerCompleteTimestamp);
    }

    @Test
    public void abort_container__sub_use_case_aborted() throws InterruptedException {

        final ContainerUseCase container = UseCase.fetch(ContainerUseCase.class);
        container.setExecutor(new TestExecutor());
        CompletedUseCase sub = UseCase.fetch(CompletedUseCase.class);
        sub.addPrimaryActor(subCompletedActor);
        subThreadManager = new ThreadManagerImpl();
        sub.setThreadManager(subThreadManager);
        container.setSubUseCase(CompletedUseCase.class);
        container.addPrimaryActor(containerActor);
        containerActor.complete = true;
        container.execute();

        sleep();

        reply1();
        replySub();
        abort();

        waitForUseCaseToFinish();

        subThreadManager.thread.join();

        assertFalse(onActorCompleteCalled);
        assertFalse(onSubCompleteCalled);
        assertTrue(onSubAbortedCalled);
        assertTrue(onSubObserverAbortedCalled);
        assertEquals(0, subOnCompleteCount);
        assertEquals(0, containerResultOnCompleteCount);
    }

    @Test
    public void undo_after_sub_use_case__sub_use_case_last_step_reset() throws InterruptedException {

        final ContainerUseCase container = UseCase.fetch(ContainerUseCase.class);
        container.setExecutor(new TestExecutor());
        CompletedUseCase sub = UseCase.fetch(CompletedUseCase.class);
        sub.addPrimaryActor(subCompletedActor);
        subThreadManager = new ThreadManagerImpl();
        sub.setThreadManager(subThreadManager);
        container.setSubUseCase(CompletedUseCase.class);
        container.addPrimaryActor(containerActor);
        containerActor.complete = true;
        container.execute();

        sleep();

        reply1();
        replySub();
        sleep();
        undo();
        replySub();
        reply2();
        replySub();

        waitForUseCaseToFinish();

        subThreadManager.thread.join();

        assertEquals(3, doBeforeSubUseCaseCalled);
        assertFalse(onSubUndoCalled);
        assertEquals(2, subDoSomethingCalled);
        assertEquals(1, subOnStartCount);
    }

    private void reply1() throws InterruptedException {
        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                sleep();
                useCaseHandle.replyWith(CK1);
            }
        });
        thread.start();

        thread.join();
    }

    private void reply2() throws InterruptedException {
        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                sleep();
                useCaseHandle.replyWithRandom(CK2);
            }
        });
        thread.start();

        thread.join();
    }

    private void replySub() throws InterruptedException {
        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                sleep();
                subHandle.replyWith(SubUseCaseTest.SK1);
            }
        });
        thread.start();

        thread.join();
    }

    private void undo() throws InterruptedException {
        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                sleep();
                useCaseHandle.undo();
            }
        });
        thread.start();

        thread.join();
    }

    private void abort() throws InterruptedException {
        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                sleep();
                useCaseHandle.abort();
            }
        });
        thread.start();

        thread.join();
    }

    private class ContainerActor implements ContainerUseCase.Actor {

        public boolean complete;

        @Override
        public void doBeforeSubUseCase() throws InterruptedException, ExecutionException, UndoException {
            doBeforeSubUseCaseCalled++;
            useCaseHandle.waitFor(this, CK1);
        }

        @Override
        public void doAfterSubUseCase() throws InterruptedException, UndoException, ExecutionException {
            useCaseHandle.waitFor(this, CK2);
        }

        @Override
        public void onStart(TectonicEvent event, UseCaseHandle handle) {
            useCaseHandle = handle;
        }

        @Override
        public void onUndo(Step step, boolean inclusive) {
            onUndoCalled = true;
        }

        @Override
        public void onComplete(TectonicEvent event) {
            containerCompleteTimestamp = System.nanoTime();
            onActorCompleteCalled = true;
        }

        @Override
        public void onAbort(TectonicEvent event) {

        }
    }

    private class SubCompletedActor implements CompletedUseCase.Actor {

        @Override
        public void onStart(TectonicEvent event, UseCaseHandle handle) {
            subHandle = handle;
            subOnStartCount++;
        }

        @Override
        public void onUndo(Step step, boolean inclusive) {
            onSubUndoCalled = true;
        }

        @Override
        public void onComplete(TectonicEvent event) {
            onSubCompleteCalled = true;
            subCompleteTimestamp = System.nanoTime();
            subOnCompleteCount++;
        }

        @Override
        public void onAbort(TectonicEvent event) {
            onSubAbortedCalled = true;
        }

        @Override
        public void doSomething() throws InterruptedException, ExecutionException, UndoException {
            subDoSomethingCalled++;
            subHandle.waitFor(this, SK1);
        }
    }

    private class TestExecutor extends SimpleTriggers {

        @Override
        public ResultActor<TectonicEvent, ?> observe(TectonicEvent contextEvent, TectonicEvent subEvent, UseCase<?> useCase) {
            return new ResultActor<TectonicEvent, Object>() {
                @Override
                public void onComplete(TectonicEvent event, Object result) {
                    onSubObserverCompleteCalled = true;
                }

                @Override
                public void onAbort(TectonicEvent event) {
                    onSubObserverAbortedCalled = true;
                }
            };
        }
    }
}
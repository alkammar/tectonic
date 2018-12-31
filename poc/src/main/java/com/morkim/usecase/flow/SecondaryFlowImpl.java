package com.morkim.usecase.flow;

import com.morkim.tectonic.flow.Step;
import com.morkim.tectonic.flow.StepFactory;
import com.morkim.tectonic.usecase.UseCaseHandle;
import com.morkim.tectonic.usecase.UndoException;
import com.morkim.tectonic.usecase.UseCase;
import com.morkim.usecase.app.UseCaseExecutor;
import com.morkim.usecase.contract.Secondary;

import java.util.UUID;

import javax.inject.Inject;

import lib.morkim.model.SecondaryModel;
import lib.morkim.uc.InvalidValueException;
import lib.morkim.uc.SecondaryUseCase;
import lib.morkim.uc.SpecificBackendError;

public class SecondaryFlowImpl implements Secondary.Flow, SecondaryUseCase.UI<UseCaseExecutor.Event> {

    private static final UUID DATA1 = UUID.randomUUID();
    private static final UUID DATA2 = UUID.randomUUID();
    private static final UUID DATA3 = UUID.randomUUID();
    private static final UUID CONFIRM = UUID.randomUUID();

    private final StepFactory stepFactory;

    private Secondary.Screen1 screen1;
    private Secondary.Screen2 screen2;
    private Secondary.Screen3 screen3;

    private UseCaseHandle handle;

    @Inject
    public SecondaryFlowImpl(StepFactory stepFactory) {

        this.stepFactory = stepFactory;
    }

    @Override
    public void onStart(UseCaseExecutor.Event event, UseCaseHandle handle) {
        this.handle = handle;
    }

    @Override
    public String askForData1() throws InterruptedException, UndoException {
        if (screen1 == null) screen1 = stepFactory.create(Secondary.Screen1.class);
        return UseCase.waitForSafe(DATA1);
    }

    @Override
    public String askForData2() throws InterruptedException, UndoException {
        if (screen2 == null) screen2 = stepFactory.create(Secondary.Screen2.class);
        return UseCase.waitForSafe(DATA2);
    }

    @Override
    public Double askForData3() throws InterruptedException, UndoException {
        if (screen3 == null) screen3 = stepFactory.create(Secondary.Screen3.class);
        return UseCase.waitForSafe(DATA3);
    }

    @Override
    public void askToConfirm() throws InterruptedException {
        UseCase.immediate(null);
    }

    @Override
    public void showError(Exception e) {
        if (e instanceof InvalidValueException) {
            UseCase.clear(DATA3);
            screen3.showError(e);
        } else if (e instanceof SpecificBackendError) {
            screen3.showError(e);
        }
    }

    @Override
    public void block() {
        screen3.block();
    }

    @Override
    public void unblock() {
        screen3.unblock();
    }

    @Override
    public void submitData1(String data1) {
        UseCase.replyWith(DATA1, data1);
    }

    @Override
    public void submitData2(String data2) {
        UseCase.replyWith(DATA2, data2);
    }

    @Override
    public void confirm(double value) {
        UseCase.replyWith(DATA3, value);
    }

    @Override
    public void goBack(Step step) {
        step.terminate();
        if (step instanceof Secondary.Screen1) handle.abort();
        else handle.undo(step);
    }

    @Override
    public void onComplete(UseCaseExecutor.Event event, SecondaryModel result) {
        screen1.terminate();
        screen2.terminate();
        screen3.terminate();

        UseCase.clear(DATA1, DATA2, DATA3);
    }

    @Override
    public void onUndo(Step step) {
        if (step == screen1) { screen1 = null; UseCase.clear(DATA1); }
        if (step == screen2) { screen2 = null; UseCase.clear(DATA1, DATA2); }
        if (step == screen3) { screen3 = null; UseCase.clear(DATA2, DATA3); }
    }

    @Override
    public void onAbort(UseCaseExecutor.Event event) {

    }
}

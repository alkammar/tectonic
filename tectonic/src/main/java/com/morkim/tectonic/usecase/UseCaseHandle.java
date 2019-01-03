package com.morkim.tectonic.usecase;

import com.morkim.tectonic.flow.Step;

import java.util.UUID;
import java.util.concurrent.ExecutionException;

public interface UseCaseHandle {

    void undo(Step step);

    void abort();

    <D> Random<D> waitForRandom(UUID key);

    <D> D waitFor(UUID key) throws ExecutionException, UndoException, InterruptedException;

    <D> D waitForSafe(UUID key) throws UndoException, InterruptedException;

    <D> D waitFor(UUID key, Class<? extends Exception>... exs) throws UnexpectedStep, InterruptedException;

    void replyWithRandom(Step step, UUID key);

    void replyWith(Step step, UUID key);

    <D> void replyWith(Step step, UUID key, D data);

    <D> void replyWithRandom(Step step, UUID key, Random<D> data);

    void clear(UUID... keys);
}

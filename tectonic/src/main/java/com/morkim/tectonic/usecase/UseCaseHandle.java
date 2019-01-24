package com.morkim.tectonic.usecase;

import com.morkim.tectonic.flow.Step;

import java.util.UUID;
import java.util.concurrent.ExecutionException;

import javax.annotation.Nonnull;

@SuppressWarnings("unused")
public interface UseCaseHandle {

    void undo();

    void abort();

    <D> Random<D> waitForRandom(UUID key);

    <D> D waitFor(@Nonnull Actor actor, UUID key) throws ExecutionException, UndoException, InterruptedException;

    <D> D waitFor(@Nonnull Actor actor, Step step, UUID key) throws ExecutionException, UndoException, InterruptedException;

    <D> D waitForSafe(@Nonnull Actor actor, UUID key) throws UndoException, InterruptedException;

    <D> D waitForSafe(@Nonnull Actor actor, Step step, UUID key) throws UndoException, InterruptedException;

    <D> D waitFor(@Nonnull Actor actor, UUID key, Runnable runnable) throws InterruptedException, ExecutionException, UndoException;

    <D> D waitFor(@Nonnull Actor actor, Step step, UUID key, Runnable runnable) throws InterruptedException, ExecutionException, UndoException;

    <D> D waitFor(@Nonnull Actor actor, UUID key, Class<? extends Exception>... exs) throws UnexpectedStep, InterruptedException;

    <D> D waitFor(@Nonnull Actor actor, Step step, UUID key, Class<? extends Exception>... exs) throws UnexpectedStep, InterruptedException;

    void replyWithRandom(UUID key);

    void replyWith(UUID key);

    <D> void replyWith(UUID key, D data);

    <D> void replyWithRandom(UUID key, Random<D> data);

    void reset();
}

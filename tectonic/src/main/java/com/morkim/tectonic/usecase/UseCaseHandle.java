package com.morkim.tectonic.usecase;

import com.morkim.tectonic.flow.Step;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ExecutionException;

import javax.annotation.Nonnull;

/**
 * A handle providing a set of functionality to interact with use case to perform some basic actions.
 * The actions are undo a step, reset a step and abort the whole use case. As well as blocking and
 * unblocking the use case by a call to any of the {@code waitFor, waitForSafe} overloads and any of
 * {@code replyWith, replyWithRandom} overloads.
 * Ideally for each version of {@code waitFor} call there should be a corresponding version of {@code replyWith}
 * call.
 */
@SuppressWarnings("unused")
public interface UseCaseHandle {

    /**
     * <p>
     * Signals the use case to undo the top step in the use case. The steps are added to the use case
     * in a form of a stack. {@link Actor#onUndo(Step, boolean)} callback of the use case actors will
     * be triggered until it reaches the next top primary actor step or the stack is cleared which will
     * cause the use case to abort.
     * </p>
     * <p>
     * For example we have a use case with steps Ps1 - Ss2 - Ps3 - Ss4 - Ss5 - Ps6 - Ps7 - Ss8 - Ps9,
     * where Ps is for primary actor step and Ss is for secondary actor step.
     * </p>
     * <p>
     * If undo called while waiting for Ss2 {@link Actor#onUndo(Step, boolean)} will be called for
     * secondary actor step Ss2 inclusive
     * and primary actor step Ps1 exclusive
     * </p>
     * <p>
     * If undo called while waiting for Ss5 {@link Actor#onUndo(Step, boolean)} will be called for
     * secondary actor step Ss5 and Ss4 inclusive
     * and primary actor step Ps3 exclusive
     * </p>
     * <p>
     * If undo called while waiting for Ps6 {@link Actor#onUndo(Step, boolean)} will be called for
     * primary actor step Ps6 inclusive
     * and secondary actor Ss5 and Ss4 inclusive
     * and primary actor Ps3 exclusive
     * </p>
     * <p>
     * If undo called while waiting for Ps8 {@link Actor#onUndo(Step, boolean)} will be called for
     * primary actor step Ps8 inclusive
     * and primary actor Ps7 exclusive
     * </p>
     * <p>
     * If undo called while waiting for Ps1 {@link Actor#onUndo(Step, boolean)} will be called for
     * primary actor step Ps1 inclusive
     * then the use case will abort calling the {@link Actor#onAbort(Object)}
     * </p>
     * <p>
     * For example we have a use case with steps Ss1 - Ss2 - Ps3
     * </p>
     * <p>
     * If undo called while waiting for Ps3 {@link Actor#onUndo(Step, boolean)} will be called for
     * primary actor step Ps3 inclusive
     * and secondary actor Ss2 and Ss1 inclusive
     * then the use case will abort calling the {@link Actor#onAbort(Object)}
     * </p>
     * <p>
     * If undo called while waiting for Ss2 {@link Actor#onUndo(Step, boolean)} will be called for
     * secondary actor Ss2 and Ss1 inclusive
     * then the use case will abort calling the {@link Actor#onAbort(Object)}
     * </p>
     * <p>
     * If undo called while waiting for Ss1 {@link Actor#onUndo(Step, boolean)} will be called for
     * secondary actor Ss1 inclusive
     * then the use case will abort calling the {@link Actor#onAbort(Object)}
     * </p>
     */
    void undo();

    /**
     * Same as {@link #undo()} specifying the step to undo to. This will undo the stack until it reaches
     * the {@code to} step. The {@code to} will be the last step to get the {@link Actor#onUndo(Step, boolean)}
     * inclusively. {@link Actor#onUndo(Step, boolean)} will be called for the previous step to the
     * {@code to} step exclusively.
     * @param to the step to undo to
     */
    void undo(Step to);

    /**
     * Signals the use case to abort resulting in a call to the {@link Actor#onAbort(Object)} method
     * for all the use case defined actors as well as any observing result actors {@link ResultActor#onAbort(Object)}
     */
    void abort();

    /**
     * Signals the use case to reset the top step data, wiping any cached values associated with
     * the step.
     */
    void reset();

    /**
     * This call does not block the use case thread and also tells it that this value will come later
     * in the future. You will need to call {@link #replyWith(UUID, Object)} with the same key in the
     * future to able to return the value.
     *
     * @param key the key to use to unblock the thread
     * @param <D> the data type of the reply value
     * @return a {@link Random} wrapper with the data value set or not set, based on the reply when
     * the use case is unblocked
     *
     * @see #replyWith(UUID, Object)
     */
    <D> Random<D> waitForRandom(UUID key);

    /**
     * Same as {@link #waitFor(Actor, Step, UUID)} but without specifying the associated step
     */
    <D> D waitFor(@Nonnull Actor actor, UUID key) throws ExecutionException, UndoException, InterruptedException;

    /**
     * Blocks the use case thread until it is unblocked by a call to any of {@code replyWith, replyWithRandom}
     * overloads or interrupting the thread. This gives the actor the ability to fetch / acquire the
     * requested data asynchronously and reply back to the use case when the data is available.
     * This technique (converting async calls to sync calls) is how the use case execute steps sequentially.
     * Here the use case will associate the {@code key} and returned data with the provided {@code actor}
     * and {@code step}, providing a means later to undo or reset a step with all its associated data
     * and calling the {@link Actor#onUndo(Step, boolean)} callback for the associated actor.
     * Passing a step is an indication that this step contains multiple pieces of data requested by
     * the use case.
     *
     * @param actor the actor requesting the block
     * @param step the step that the requested data is associated with. If null is passed the requested
     *             data will be associated with an anonymous step.
     * @param key the key to use to unblock the thread
     * @param <D> the data type of the reply value
     * @return the data value the use case is expecting
     * @throws ExecutionException thrown if {@code ExecutionException} is set when the use case is unblocked
     * @throws UndoException thrown if {@code UndoException} is set when the use case is unblocked.
     * For instance this is how {@code undo} implementation work
     * @throws InterruptedException thrown if {@code InterruptedException} is set when the use case
     * is unblocked or the use case thread is interrupted
     *
     * @see #replyWith(UUID, Object)
     * @see #replyWithRandom(UUID, Random)
     */
    <D> D waitFor(@Nonnull Actor actor, Step step, UUID key) throws ExecutionException, UndoException, InterruptedException;

    /**
     * Same as {@link #waitForSafe(Actor, Step, UUID)} but without specifying the associated step
     */
    <D> D waitForSafe(@Nonnull Actor actor, UUID key) throws UndoException, InterruptedException;

    /**
     * Version of {@link #waitFor(Actor, Step, UUID)} but the {@code ExecutionException} is never thrown.
     * This is typically used when blocking for UI responses and you know for sure that no exceptions
     * will be thrown during that blockage.
     */
    <D> D waitForSafe(@Nonnull Actor actor, Step step, UUID key) throws UndoException, InterruptedException;

    /**
     * Same as {@link #waitFor(Actor, Step, UUID, Runnable)} but uses {@link Executable} instead {@link Runnable} to allow passing of UUID without storing it in
     * the calling code
     */
    <D> D waitForSafe(@Nonnull Actor actor, Step step, UUID key, Executable executable) throws InterruptedException, UndoException;

    /**
     * Same as {@link #waitFor(Actor, Step, UUID, Runnable)} but without specifying the associated step
     */
    <D> D waitFor(@Nonnull Actor actor, UUID key, Runnable runnable) throws InterruptedException, ExecutionException, UndoException;

    /**
     * Version of {@link #waitFor(Actor, Step, UUID)} but waiting for a runnable to unblock the use case.
     * Inside your runnable implementation you should call one of the {@code replyWith} methods to
     * or call {@link #undo()}, {@link #reset()} or {@link #abort()} to unblock the use case thread.
     *
     * @param runnable the runnable to be executed
     */
    <D> D waitFor(@Nonnull Actor actor, Step step, UUID key, Runnable runnable) throws InterruptedException, ExecutionException, UndoException;

    /**
     * Same as {@link #waitFor(Actor, Step, UUID, Class[])} but without specifying the associated step
     */
    <D> D waitFor(@Nonnull Actor actor, UUID key, Class<? extends Exception>... exs) throws UnexpectedStep, InterruptedException;

    /**
     * Version of {@link #waitFor(Actor, Step, UUID)} but waiting for one of exceptions {@code exs}
     * to be set when unblocking the use case thread.
     *
     * @param exs array of expected exceptions
     * @throws UnexpectedStep a wrapper for the set exception
     */
    <D> D waitFor(@Nonnull Actor actor, Step step, UUID key, Class<? extends Exception>... exs) throws UnexpectedStep, InterruptedException;

    /**
     * Same as {@link #replyWithRandom(UUID, Random)} but without data. Useful if blocking for an action
     * and not a value.
     */
    void replyWithRandom(UUID key);

    /**
     * Same as {@link #replyWith(UUID, Object)} but without data. Useful if blocking for an action
     * and not a value.
     */
    void replyWith(UUID key);

    /**
     * Unblocks the use case thread that is blocked by a call to any of {@code waitFor} of {@code waitForSafe}
     * methods with that {@code key} and setting the {@code data} value to be returned by it.
     *
     * @param key the key for the blocking request
     * @param data the data value that is going to be returned by {@code waitFor} method
     * @param <D> the value data type, this is should match the one defined in the {@code waitFor} method
     */
    <D> void replyWith(UUID key, D data);

    <D> void submitBatch(Map.Entry<UUID, D>... batch);

    /**
     * Unblocks the use case thread that is blocked by a call to any of {@code waitFor} of {@code waitForSafe}
     * methods with that {@code key} and setting the {@code data} value to be returned by it.
     * This call forces the use to reevaluate any previous {@link #waitForRandom(UUID)} calls. So if
     * you were waiting for one or more {@link Random} data you should have already called {@link #replyWith(UUID, Object)}
     * for each {@link #waitForRandom(UUID)} calls.
     *
     * @param key the key for the blocking request
     * @param data the data value that is going to be returned by {@code waitFor} method
     * @param <D> the value data type, this is should match the one defined in the {@code waitFor} method
     *
     * @see #waitFor(Actor, Step, UUID)
     * @see #waitFor(Actor, Step, UUID, Runnable)
     * @see #waitFor(Actor, Step, UUID, Class[])
     * @see #waitForRandom(UUID)
     */
    <D> void replyWithRandom(UUID key, Random<D> data);

    /**
     * Same as {@link #immediate(Actor, Step, UUID, Object)} but without specifying the associated step
     * and data
     */
    <D> D immediate(@Nonnull Actor actor, UUID key);

    /**
     * Same as {@link #immediate(Actor, Step, UUID, Object)} but without specifying the associated step
     */
    <D> D immediate(@Nonnull Actor actor, UUID key, D data);

    /**
     * Same as {@link #immediate(Actor, Step, UUID, Object)} but without data
     */
    <D> D immediate(@Nonnull Actor actor, Step step, UUID key);

    /**
     * Does not block the use case thread, but creates a step in the cache stack so undoing a use case
     * can stop at this step.
     *
     * @param actor actor the actor requesting the block
     * @param step the step that the requested data is associated with. If null is passed the requested
     *             data will be associated with an anonymous step.
     * @param key the key for the step
     * @param data the data value
     * @param <D> the value data type
     * @return the data entered in {@code data}
     */
    <D> D immediate(@Nonnull Actor actor, Step step, UUID key, D data);

    /**
     * Returns the instance ID of this use case. If the use case is run as part of multiple instances
     * of the same use case, this should return a unique ID per use case. The ID is passed while creating
     * the use case, either by calling {@link UseCase#fetch(Class, String)} or building with {@link Builder}
     * and using {@link Builder#instanceId(String)}. If the use case is not part of multiple instances,
     * this will return empty string.
     *
     * @return the use case instance ID or empty string if not running as part of multiple instances
     */
    String getInstanceId();
}

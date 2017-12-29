package com.morkim.tectonic;


import java.util.List;

@SuppressWarnings("WeakerAccess")
public interface UseCaseListener<Rs extends Result> {

	/**
	 * Called when a use case is started. This is called once for each subscriber in the life time
	 * of a use case. If the use case is completed and then started again this will be called again.
	 * Typically this is used to start a progress indicator.
	 */
	void onStart();

	/**
	 * Called to send result updates of the use case execution. This can be called multiple times
	 * in the life time of a use case
	 *
	 * @param result The update
	 */
	void onUpdate(Rs result);

	/**
	 * Called once a use case has concluded its execution.
	 * Typically this is used to dismiss a progress indicator.
	 */
	void onComplete();

	/**
	 * Called when the use case is cancelled.
	 */
	void onCancel();

	/**
	 * Called when the use case requires an action to proceed with its execution. The code is passed
	 * to indicate which action is required. The last subscribed listener will be the first to be called.
	 * If a listener returns true, this means the action is handled at that listener and other listeners
	 * will not receive this callback.
	 *
	 * @param codes The codes for the required actions.
	 * @return true if the listener is handling the required action.
	 */
	boolean onActionRequired(List<Integer> codes);

    boolean onError(Throwable throwable);

	void onUndone();
}

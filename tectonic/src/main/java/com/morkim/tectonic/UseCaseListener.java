package com.morkim.tectonic;


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
	 * Called when the use case requires an input to proceed with its execution. The code is passed
	 * to indicate which input is required.
	 *
	 * @param code The code for the required input.
	 */
	void onInputRequired(int code);
}

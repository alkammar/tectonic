package com.morkim.tectonic;

/**
 * The base class for a request to be sent to a use case
 */
@SuppressWarnings("UnusedParameters")
public class Request {

	@SuppressWarnings("WeakerAccess")
	public static final int NO_ID = 0;

	@SuppressWarnings("unused")
	public static class Builder<T extends Builder> {

		public Builder() {}

		public Request build() { return new Request(this); }
	}

	protected Request(Builder builder) {

	}

	/**
	 * Override this to return a unique ID for your request object. This is used if multiple result
	 * caching is required for a single use case. For example you want to run the same use case for
	 * different requests and you want to cache the results of the different executions.
	 *
	 * @return The request ID
	 */
	protected int id() {
		return NO_ID;
	}
}

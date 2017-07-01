package com.morkim.tectonic;


public class Request {

	public static final int NO_ID = 0;

	public static class Builder<T extends Builder> {

		public Builder() {}

		public Request build() { return new Request(this); }
	}

	protected Request(Builder builder) {

	}

	protected int id() {
		return NO_ID;
	}
}

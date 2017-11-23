package com.morkim.tectonic.entities;

import com.morkim.tectonic.Request;


public class PendingActionRequest extends Request {

	String input1;
	String input2;

	private PendingActionRequest(Builder builder) {
		super(builder);

		this.input1 = builder.input1;
		this.input2 = builder.input2;
	}

	public static class Builder extends Request.Builder<Builder> {

		private String input1 = "";
		private String input2 = "";

		public Builder input1(String input1) {
			this.input1 = input1;

			return this;
		}

		public Builder input2(String input2) {
			this.input2 = input2;

			return this;
		}

		public PendingActionRequest build() { return new PendingActionRequest(this); }
	}
}

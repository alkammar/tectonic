package com.morkim.tectonic.entities;

import com.morkim.tectonic.Request;


public class PendingActionRequest extends Request {

	private PendingActionRequest(Builder builder) {
		super(builder);
	}

	public static class Builder extends Request.Builder<Builder> {

		public PendingActionRequest build() { return new PendingActionRequest(this); }
	}
}

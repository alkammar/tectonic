package com.morkim.tectonic.entities;


import com.morkim.tectonic.Request;

public class CancelledPrerequisiteTestUseCase extends TestUseCase {

	@Override
	protected void onExecute(Request request) {
		abort();
	}
}

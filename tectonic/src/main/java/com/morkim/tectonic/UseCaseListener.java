package com.morkim.tectonic;


public interface UseCaseListener<Rs extends Result> {

	void onStart();

	void onUpdate(Rs result);

	void onComplete();

	void onCancel();

	void onInputRequired(int code);
}

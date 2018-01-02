package com.morkim.tectonic;


import java.util.List;

public class SimpleUseCaseListener<Rs extends Result> implements UseCaseListener<Rs> {

	@Override
	public void onStart() {

	}

	@Override
	public void onUpdate(Rs result) {

	}

	@Override
	public void onComplete() {

	}

	@Override
	public void onCancel() {

	}

	@Override
	public void onActionRequired(List<Integer> codes) {

    }

	@Override
	public boolean onError(Throwable throwable) {
		return false;
	}

	@Override
	public void onUndone(Rs oldResult) {

	}
}

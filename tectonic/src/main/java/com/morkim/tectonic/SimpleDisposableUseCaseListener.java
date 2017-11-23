package com.morkim.tectonic;


import java.util.List;

public class SimpleDisposableUseCaseListener<Rs extends Result> implements DisposableUseCaseListener<Rs> {

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
	public boolean onInputRequired(List<Integer> codes) {

        return false;
    }
}

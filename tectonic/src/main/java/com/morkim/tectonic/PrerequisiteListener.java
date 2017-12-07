package com.morkim.tectonic;

import java.util.List;

@SuppressWarnings("unchecked")
class PrerequisiteListener implements DisposableUseCaseListener {

    private UseCaseListener listener = new SimpleDisposableUseCaseListener();

    PrerequisiteListener(UseCaseListener listener) {

        if (listener != null)
            this.listener = listener;
    }

    @Override
    public void onStart() {
        listener.onStart();
    }

    @Override
    public void onUpdate(Result result) {
        listener.onUpdate(result);
    }

    @Override
    public void onComplete() {
        listener.onComplete();
    }

    @Override
    public void onCancel() {
        listener.onCancel();
    }

    @Override
    public boolean onError(Throwable throwable) {
        return listener.onError(throwable);
    }

    @Override
    public boolean onInputRequired(List codes) {
        return listener.onInputRequired(codes);
    }

}

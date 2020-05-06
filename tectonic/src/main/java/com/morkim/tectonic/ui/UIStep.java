package com.morkim.tectonic.ui;

import android.os.Bundle;

import com.morkim.tectonic.flow.Step;

@SuppressWarnings("unused")
public interface UIStep extends Step {

    UIStep onNewReference(Bundle data);

    void showLoading();

    void dismissLoading();

    void showError(Exception e);

    void showError(int e);

    void setOnGoBackListener(OnGoBackListener onGoBackListener);

    interface OnGoBackListener {

        void onGoBack();
    }

    void setOnTerminatedListener(OnTerminatedListener onTerminatedListener);

    interface OnTerminatedListener {

        void onTerminated(Class<?> cls);
    }
}
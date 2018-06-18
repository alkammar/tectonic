package com.morkim.tectonic.simplified;

import com.morkim.tectonic.flow.Step;

public interface PrimaryActor {

    void onStart(UseCaseHandle handle);

    void onUndo(Step step);

    void onAbort();
}

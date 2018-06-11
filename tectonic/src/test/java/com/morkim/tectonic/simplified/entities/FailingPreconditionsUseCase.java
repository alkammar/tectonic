package com.morkim.tectonic.simplified.entities;

import com.morkim.tectonic.simplified.UseCase;

public class FailingPreconditionsUseCase extends SimpleUseCase {

    @Override
    protected boolean onCheckPreconditions() {
        //noinspection PointlessBooleanExpression
        return super.onCheckPreconditions() && false;
    }
}

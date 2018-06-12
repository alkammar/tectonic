package com.morkim.tectonic.simplified.entities;

public class FailingPreconditionsUseCase extends SimpleUseCase {

    @Override
    protected boolean onCheckPreconditions() {
        //noinspection PointlessBooleanExpression
        return super.onCheckPreconditions() && false;
    }
}

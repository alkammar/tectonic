package com.morkim.tectonic.simplified.entities;

public class CompletedPreconditionsUseCase extends SimpleUseCase {

    @Override
    protected boolean onCheckPreconditions() {
        //noinspection PointlessBooleanExpression
        return super.onCheckPreconditions() && true;
    }
}

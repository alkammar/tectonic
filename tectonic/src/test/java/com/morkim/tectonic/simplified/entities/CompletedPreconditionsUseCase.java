package com.morkim.tectonic.simplified.entities;

import com.morkim.tectonic.simplified.UseCase;

public class CompletedPreconditionsUseCase extends SimpleUseCase {

    @Override
    protected boolean onCheckPreconditions() {
        //noinspection PointlessBooleanExpression
        return super.onCheckPreconditions() && true;
    }
}

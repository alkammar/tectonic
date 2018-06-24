package com.morkim.tectonic.simplified.entities;

import java.util.Set;

public class FailingPreconditionsUseCase extends SimpleUseCase {

    private static final Integer FAILING_EVENT = 12;

    @Override
    protected void onAddPreconditions(Set<Integer> events) {
        //noinspection PointlessBooleanExpression
//        return super.onAddPreconditions(events) && false;
        events.add(FAILING_EVENT);
    }
}

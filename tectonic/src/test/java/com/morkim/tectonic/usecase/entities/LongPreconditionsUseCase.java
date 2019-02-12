package com.morkim.tectonic.usecase.entities;

import com.morkim.tectonic.usecase.TectonicEvent;
import com.morkim.tectonic.usecase.UseCase;

import java.util.Set;

public class LongPreconditionsUseCase extends SimpleUseCase {

    public static final TectonicEvent FAILING_EVENT = new TectonicEvent() {};

    @Override
    protected void onAddPreconditions(Set<Class<? extends UseCase>> useCases) {
        super.onAddPreconditions(useCases);

        useCases.add(InterruptableUseCase.class);
    }
}

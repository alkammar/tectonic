package com.morkim.tectonic.usecase;

public interface PreconditionActor<E extends TectonicEvent> {
    
    void onComplete(E event);
    
    void onAbort(E event);
}

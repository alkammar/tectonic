package com.morkim.tectonic.usecase;

public interface PreconditionActor<E> {
    
    void onComplete(E event);
    
    void onAbort(E event);
}

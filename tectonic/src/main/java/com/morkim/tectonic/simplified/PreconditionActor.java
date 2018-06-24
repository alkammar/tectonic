package com.morkim.tectonic.simplified;

public interface PreconditionActor<E> {
    
    void onComplete(E event);
    
    void onAbort(E event);
}

package com.morkim.tectonic.simplified;

public interface PreconditionActor<E, R> {
    
    void onComplete(E event, R result);
    
    void onAbort(E event);
}

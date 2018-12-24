package com.morkim.tectonic.usecase;

public interface ResultActor<E, R> {
    
    void onComplete(E event, R result);
    
    void onAbort(E event);
}

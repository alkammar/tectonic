package com.morkim.tectonic.simplified;

public interface PrimaryActor {

    void onStart(PrimaryActor primaryActor);

    void onAbort(PrimaryActor primaryActor);
}

package com.morkim.tectonic.flow;

public interface StepListener {

    <S> void onCreated(S step);

    <S> void onCreated(S step, S impl);
}

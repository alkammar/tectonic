package com.morkim.tectonic.simplified.entities;

public class StepData {

    private int accessCount;

    public void access() {
        accessCount++;
    }

    public int getAccessCount() {
        return accessCount;
    }
}

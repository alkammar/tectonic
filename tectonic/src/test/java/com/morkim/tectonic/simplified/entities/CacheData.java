package com.morkim.tectonic.simplified.entities;

public class CacheData {

    private int accessCount;

    public void access() {
        accessCount++;
    }

    public int getAccessCount() {
        return accessCount;
    }
}

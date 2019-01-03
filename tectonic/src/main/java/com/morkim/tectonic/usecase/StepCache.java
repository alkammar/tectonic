package com.morkim.tectonic.usecase;

import com.morkim.tectonic.flow.Step;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

public class StepCache {

    private Map<Step, Set<UUID>> steps = new HashMap<>();
    private Map<UUID, Object> values = new HashMap<>();

    void put(Step step, UUID key, Object value) {
        Set<UUID> stepKeys = steps.get(step);
        if (stepKeys == null) stepKeys = new HashSet<>();
        stepKeys.add(key);
        steps.put(step, stepKeys);
        values.put(key, value);
    }

    boolean containsKey(UUID key) {
        return values.containsKey(key);
    }

    @SuppressWarnings("unchecked")
    <D> D get(UUID key) {
        return (D) values.get(key);
    }

    void remove(UUID key) {
        values.remove(key);
    }

    void clear() {
        steps.clear();
        values.clear();
    }

    void remove(Step step) {
        Set<UUID> keys = steps.get(step);
        for (UUID key : keys)
            values.remove(key);

        steps.remove(step);
    }
}

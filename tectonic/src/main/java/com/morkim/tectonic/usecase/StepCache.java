package com.morkim.tectonic.usecase;

import com.morkim.tectonic.flow.Step;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.Stack;
import java.util.UUID;

public class StepCache {

    private Map<Step, Set<UUID>> keys = new HashMap<>();
    private Map<Step, Actor> actors = new HashMap<>();
    private Stack<Step> stack = new Stack<>();

    private Map<UUID, Object> values = new HashMap<>();
    private Map<UUID, Synchronizer> synchronizers = new HashMap<>();
    private Map<Class<? extends UseCase>, UUID> subUseCaseKeys = new HashMap<>();
    private Map<Step, Class<? extends UseCase>> subUseCases = new HashMap<>();

    <D> void put(Actor actor, Step step, UUID key, Synchronizer<D> synchronizer) {
        synchronizers.put(key, synchronizer);
        Set<UUID> stepKeys = keys.get(step);
        if (stepKeys == null) stepKeys = new HashSet<>();
        stepKeys.add(key);
        if (keys.put(step, stepKeys) == null)
            stack.add(step);
        actors.put(step, actor);
    }

    void put(UUID key, Object value) {
        values.put(key, value);
    }

    boolean containsKey(UUID key) {
        return values.containsKey(key);
    }

    @SuppressWarnings("unchecked")
    <D> D getValue(UUID key) {
        return (D) values.get(key);
    }

    void remove(UUID key) {
        values.remove(key);
    }

    void clear() {
        keys.clear();
        actors.clear();
        stack.clear();
        values.clear();
        synchronizers.clear();
    }

    Synchronizer getSynchronizer(UUID key) {
        return synchronizers.get(key);
    }

    boolean isEmpty() {
        return keys.isEmpty();
    }

    Step peak() {
        return stack.isEmpty() ? null : stack.peek();
    }

    Actor pop() {
        Step step = stack.isEmpty() ? null : stack.pop();
        Actor actor = actors.get(step);
        remove(step);
        return actor;
    }

    <r> r push(Actor actor, Step step, UUID key, r value) {

        Set<UUID> stepKeys = keys.get(step);
        if (stepKeys == null) stepKeys = new HashSet<>();
        stepKeys.add(key);
        if (keys.put(step, stepKeys) == null)
            stack.push(step);
        this.actors.put(step, actor);

        values.put(key, value);

        return value;
    }

    private void remove(Step step) {
        removeKeys(step);
        clearSubUseCase(step);
        this.keys.remove(step);
        this.actors.remove(step);
    }

    private void removeKeys(Step step) {
        Set<UUID> keys = this.keys.get(step);
        if (keys != null)
            for (UUID key : keys)
                values.remove(key);
    }

    private void clearSubUseCase(Step step) {
        Class<? extends UseCase> subUseCase = this.subUseCases.remove(step);
        if (subUseCase != null) subUseCaseKeys.remove(subUseCase);
    }

    Actor getActor(Step step) {
        return actors.get(step);
    }

    void reset(Step step) {
        removeKeys(step);
        clearSubUseCase(step);
    }

    boolean contains(Class<? extends UseCase> cls) {
        return subUseCaseKeys.containsKey(cls);
    }

    @SuppressWarnings("unchecked")
    <r> r getValue(Class<? extends UseCase<r>> cls) {
        UUID key = subUseCaseKeys.get(cls);
        return key != null ? (r) getValue(key) : null;
    }

    void put(Class<? extends UseCase> cls, Step step, UUID key) {
        subUseCaseKeys.put(cls, key);
        subUseCases.put(step, cls);
    }
}

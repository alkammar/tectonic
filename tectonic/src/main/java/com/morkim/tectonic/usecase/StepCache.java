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
    private Map<UUID, Action> actions = new HashMap<>();

    <D> void put(Actor actor, Step step, UUID key, Action<D> action) {
        actions.put(key, action);
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
        actions.clear();
    }

    Action getAction(UUID key) {
        return actions.get(key);
    }
//
//    Actor getActor(Step step) {
//        return actors.get(step);
//    }

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

    private void remove(Step step) {
        removeKeys(step);
        this.keys.remove(step);
        this.actors.remove(step);
    }

    private void removeKeys(Step step) {
        Set<UUID> keys = this.keys.get(step);
        if (keys != null)
            for (UUID key : keys)
                values.remove(key);
    }

    Actor getActor(Step step) {
        return actors.get(step);
    }

    void reset(Step step) {
        removeKeys(step);
    }
}

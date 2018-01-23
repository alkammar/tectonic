package com.morkim.tectonic;

import android.annotation.SuppressLint;

import java.util.HashMap;
import java.util.Map;
import java.util.Stack;

class ResultStack {

    private Stack<Integer> stack = new Stack<>();
    @SuppressLint("UseSparseArrays")
    private Map<Integer, Result> map = new HashMap<>();

    void put(int id, Result result) {

        if (map.get(id) == null) {
            map.put(id, result);
        } else {
            stack.removeElement(id);
        }

        stack.push(id);
    }

    Result get(int id) {
        return map.get(id);
    }

    Result pop() {

        Integer id = stack.pop();
        Result result = map.get(id);
        map.remove(id);

        return result;
    }

    Result getLast() {
        return map.get(stack.lastElement());
    }
}

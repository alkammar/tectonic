package com.morkim.tectonic;

public interface Precondition {

    Precondition TRUE_PRECONDITION = new Precondition() {
        @Override
        public boolean onEvaluate() {
            return true;
        }
    };

    boolean onEvaluate();
}

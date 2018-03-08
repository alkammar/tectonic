package com.morkim.tectonic;

public interface PreAction {

    PreAction NO_ACTION = new PreAction() {
        @Override
        public void onBlockExecute() {

        }
    };

    void onBlockExecute();
}

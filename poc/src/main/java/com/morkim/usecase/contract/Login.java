package com.morkim.usecase.contract;

import com.morkim.tectonic.flow.Step;

public interface Login {

    interface Flow {

        void submit(String password);

        void notRegistered();
    }

    interface Screen extends Step {

        void handle(Exception e);
    }
}

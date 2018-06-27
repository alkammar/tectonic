package com.morkim.usecase.contract;

import com.morkim.tectonic.flow.Step;

public interface Login {

    interface Flow {

        void submit(String password);
    }

    interface Screen extends Step {

        void handle(Exception e);
    }
}

package com.morkim.usecase.contract;

import com.morkim.tectonic.flow.Step;

public interface Registration {

    interface Flow {

        void submit(String email, String password);

        void submit(String mobile);

        void goBack(Step step);
    }

    interface Step1 extends Step {

        void handle(Exception e);
    }

    interface Step2 extends Step {

    }
}

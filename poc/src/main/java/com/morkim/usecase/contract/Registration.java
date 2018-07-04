package com.morkim.usecase.contract;

import com.morkim.tectonic.flow.Step;

public interface Registration {

    interface Flow {

        void submitPassword(String password);

        void next(String email, String password, String passwordConfirm);

        void submit(String mobile);

        void goBack(Step step);
    }

    interface Step1 extends Step {

        void showError(int e);

        void updatePasswordStrength(int strength);

        void showError(Exception e);
    }

    interface Step2 extends Step {

        void showError(int e);

        void showError(Exception e);
    }
}

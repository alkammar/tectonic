package com.morkim.usecase.contract;

import com.morkim.tectonic.flow.Step;

public interface Secondary {

    interface Flow {

        void submitData1(String data2);

        void submitData2(String data2);

        void goBack(Step step);

        void confirm(double value);
    }

    interface Screen1 extends Step {

    }

    interface Screen2 extends Step {

    }

    interface Screen3 extends Step {

        void block();

        void unblock();

        void showError(Exception e);
    }
}

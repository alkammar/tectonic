package com.morkim.usecase.contract.login;

public interface Login {

    interface Flow {

        void submit(String password);
    }

    interface Screen {

        void handle(Exception e);

        void finish();
    }
}

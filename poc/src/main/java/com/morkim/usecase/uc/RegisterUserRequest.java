package com.morkim.usecase.uc;

import com.morkim.tectonic.Request;

public class RegisterUserRequest extends Request {

    public final String email;
    public String password;
    public final String mobile;

    private RegisterUserRequest(Builder builder) {
        super(builder);

        email = builder.email;
        password = builder.password;
        mobile = builder.mobile;
    }

    public static class Builder extends Request.Builder<Builder> {

        private String email = "";
        private String password = "";
        private String mobile = "";

        public Builder email(String email) {
            this.email = email;

            return this;
        }

        public Builder password(String password) {
            this.password = password;

            return this;
        }

        public Builder mobile(String mobile) {
            this.mobile = mobile;

            return this;
        }

        public RegisterUserRequest build() {
            return new RegisterUserRequest(this);
        }
    }
}

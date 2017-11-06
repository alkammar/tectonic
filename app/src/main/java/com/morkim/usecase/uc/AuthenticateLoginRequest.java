package com.morkim.usecase.uc;

import com.morkim.tectonic.Request;

public class AuthenticateLoginRequest extends Request {

    public String password;

    private AuthenticateLoginRequest(Builder builder) {
        super(builder);

        password = builder.password;
    }

    public static class Builder extends Request.Builder<Builder> {

        private String password = "";

        public Builder password(String password) {
            this.password = password;

            return this;
        }

        public AuthenticateLoginRequest build() {
            return new AuthenticateLoginRequest(this);
        }
    }
}

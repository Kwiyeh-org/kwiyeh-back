package com.kwiyeh.back.utils;
public class VerifyCodeRequest{

    private String email;
    private String forgetPasswordCode;

    public String getEmail() {
        return email;
    }
    public void setEmail(String email) {
        this.email = email;
    }
    public String getForgetPasswordCode() {
        return forgetPasswordCode;
    }
    public void setForgetPasswordCode(String forgetPasswordCode) {
        this.forgetPasswordCode = forgetPasswordCode;
    }
}

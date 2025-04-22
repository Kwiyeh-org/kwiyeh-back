package com.kwiyeh.back.utils;
public class PasswordResetRequest{

    private String email;
    private String forgetPasswordCode;
    private String password;

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
    public String getPassword() {
        return password;
    }
    public void setPassword(String password) {
        this.password = password;
    }
}

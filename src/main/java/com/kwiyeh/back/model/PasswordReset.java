package com.kwiyeh.back.model;

import java.util.Date;

import jakarta.persistence.Entity;

@Entity
public class PasswordReset {
    private String forgetPasswordCode;
    private Date createdAt;

    public String getForgetPasswordCode() {
        return forgetPasswordCode;
    }
    public void setForgetPasswordCode(String forgetPasswordCode) {
        this.forgetPasswordCode = forgetPasswordCode;
    }
    public Date getCreatedAt() {
        return createdAt;
    }
    public void setCreatedAt(Date createdAt) {
        this.createdAt = createdAt;
    }
}

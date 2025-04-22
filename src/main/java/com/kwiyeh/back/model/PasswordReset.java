package com.kwiyeh.back.model;

import java.util.Date;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class PasswordReset {
    private String forgetPasswordCode;
    private Date createdAt;
}

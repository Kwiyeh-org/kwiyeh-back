package com.kwiyeh.back.utils;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class PasswordResetRequest{
    private String email;
    private String forgetPasswordCode;
    private String password;
}

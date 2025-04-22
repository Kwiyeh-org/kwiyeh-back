package com.kwiyeh.back.utils;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class SignUpRequest {
    private String fullName;
    private String email;
    private String phoneNumber;
    private String password;
}

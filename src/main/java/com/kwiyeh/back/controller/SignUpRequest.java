package com.kwiyeh.back.controller;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
class SignUpRequest {
    private String fullName;
    private String email;
    private String phoneNumber;
    private String password;
}

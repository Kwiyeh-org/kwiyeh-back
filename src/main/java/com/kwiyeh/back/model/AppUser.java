package com.kwiyeh.back.model;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
public class AppUser {
    @Id
    //@GeneratedValue(strategy = GenerationType.AUTO)  //to generate the document if automatically
    private String uid;
    private String email;
    private String fullName;
    private String phoneNumber;
}

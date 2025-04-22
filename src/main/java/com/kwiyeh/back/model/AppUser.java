package com.kwiyeh.back.model;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
@Entity
public class AppUser {
    @Id
    //@GeneratedValue(strategy = GenerationType.AUTO)  //to generate the document if automatically
    private String uid;
    private String email;
    private String fullName;
    private String phoneNumber;

    public String getUid() {
        return uid;
    }
    public void setUid(String uid) {
        this.uid = uid;
    }
    public String getEmail() {
        return email;
    }
    public void setEmail(String email) {
        this.email = email;
    }
    public String getFullName() {
        return fullName;
    }
    public void setFullName(String fullName) {
        this.fullName = fullName;
    }
    public String getPhoneNumber() {
        return phoneNumber;
    }
    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }
}

package com.kwiyeh.back.model;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
@Entity
public class AppUser {
    @Id
    private String uid;
    private String email;
    private String fullName;
    private String phoneNumber;
    private String type;

    public AppUser( String uid,String email, String fullName, String phoneNumber, String type) {
        this.uid = uid;
        this.email = email;
        this.fullName = fullName;
        this.phoneNumber = phoneNumber;
        this.type = type;
    }

    public AppUser() {
        // Default constructor for JPA
    }

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
    public String getType() {
        return type;
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
    public void setType(String type) {
        this.type = type;
    }

    public String toJson() {
        return "{" +
                "\"uid\":\"" + uid + "\"," +
                "\"email\":\"" + email + "\"," +
                "\"fullName\":\"" + fullName + "\"," +
                "\"phoneNumber\":\"" + phoneNumber + "\"," +
                "\"type\":\"" + type + "\"" +
                "}";
    }
}

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
    private String role;
    private String location;

    public AppUser( String uid,String email, String fullName, String phoneNumber, String role) {
        this.uid = uid;
        this.email = email;
        this.fullName = fullName;
        this.phoneNumber = phoneNumber;
        this.role = role;
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
    public String getRole() {
        return role;
    }
    public String getLocation() {
        return location;
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
    public void setRole(String role) {
        this.role = role;
    }
    public void setLocation(String location) {
        this.location = location;
    }
}

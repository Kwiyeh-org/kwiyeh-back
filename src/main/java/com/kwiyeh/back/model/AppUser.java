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

    // Client-specific fields
    private String clientImageUrl;
    private String clientLocation;

    // Talent-specific fields
    private String talentImageUrl;
    private String talentLocation;
    private String talentCategory;
    private String talentDescription;
    private String pricing;
    private String availability;

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

    // Client fields
    public String getClientImageUrl() {
        return clientImageUrl;
    }
    public void setClientImageUrl(String clientImageUrl) {
        this.clientImageUrl = clientImageUrl;
    }
    public String getClientLocation() {
        return clientLocation;
    }
    public void setClientLocation(String clientLocation) {
        this.clientLocation = clientLocation;
    }

    // Talent fields
    public String getTalentImageUrl() {
        return talentImageUrl;
    }
    public void setTalentImageUrl(String talentImageUrl) {
        this.talentImageUrl = talentImageUrl;
    }
    public String getTalentLocation() {
        return talentLocation;
    }
    public void setTalentLocation(String talentLocation) {
        this.talentLocation = talentLocation;
    }
    public String getTalentCategory() {
        return talentCategory;
    }
    public void setTalentCategory(String talentCategory) {
        this.talentCategory = talentCategory;
    }
    public String getTalentDescription() {
        return talentDescription;
    }
    public void setTalentDescription(String talentDescription) {
        this.talentDescription = talentDescription;
    }
    public String getPricing() {
        return pricing;
    }
    public void setPricing(String pricing) {
        this.pricing = pricing;
    }
    public String getAvailability() {
        return availability;
    }
    public void setAvailability(String availability) {
        this.availability = availability;
    }

    // Role-specific JSON
    public String toClientJson() {
        return "{" +
                "\"uid\":\"" + uid + "\"," +
                "\"email\":\"" + email + "\"," +
                "\"fullName\":\"" + fullName + "\"," +
                "\"phoneNumber\":\"" + phoneNumber + "\"," +
                "\"role\":\"client\"," +
                "\"clientImageUrl\":\"" + clientImageUrl + "\"," +
                "\"location\":\"" + clientLocation + "\"" +
                "}";
    }
    public String toTalentJson() {
        return "{" +
                "\"uid\":\"" + uid + "\"," +
                "\"email\":\"" + email + "\"," +
                "\"fullName\":\"" + fullName + "\"," +
                "\"phoneNumber\":\"" + phoneNumber + "\"," +
                "\"role\":\"talent\"," +
                "\"talentImageUrl\":\"" + talentImageUrl + "\"," +
                "\"location\":\"" + talentLocation + "\"," +
                "\"talentCategory\":\"" + talentCategory + "\"," +
                "\"talentDescription\":\"" + talentDescription + "\"," +
                "\"pricing\":\"" + pricing + "\"," +
                "\"availability\":\"" + availability + "\"" +
                "}";
    }

    @Override
    public String toString() {
        return "AppUser{" +
                "uid='" + uid + '\'' +
                ", email='" + email + '\'' +
                ", fullName='" + fullName + '\'' +
                ", phoneNumber='" + phoneNumber + '\'' +
                ", role='" + role + '\'' +
                ", location='" + location + '\'' +
                ", clientImageUrl='" + clientImageUrl + '\'' +
                ", clientLocation='" + clientLocation + '\'' +
                ", talentImageUrl='" + talentImageUrl + '\'' +
                ", talentLocation='" + talentLocation + '\'' +
                ", talentCategory='" + talentCategory + '\'' +
                ", talentDescription='" + talentDescription + '\'' +
                ", pricing='" + pricing + '\'' +
                ", availability='" + availability + '\'' +
                '}';
    }
}

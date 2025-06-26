package com.kwiyeh.back.utils;

public class TalentInfo {
    private String uid;
    private String email;
    private String fullName;
    private String phoneNumber;
    private String role;
    private String talentName;
    private String talentDescription;
    private String talentCategory;
    private String location;
    private String talentImageUrl;
    private String pricing;
    private String availability;
    private float rating;

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

    public String getTalentName() {
        return talentName;
    }

    public void setTalentName(String talentName) {
        this.talentName = talentName;
    }

    public String getTalentDescription() {
        return talentDescription;
    }

    public void setTalentDescription(String talentDescription) {
        this.talentDescription = talentDescription;
    }

    public String getTalentCategory() {
        return talentCategory;
    }

    public void setTalentCategory(String talentCategory) {
        this.talentCategory = talentCategory;
    }

    public String getTalentImageUrl() {
        return talentImageUrl;
    }

    public void setTalentImageUrl(String talentImageUrl) {
        this.talentImageUrl = talentImageUrl;
    }

    public String getLocation() {
        return location;
    }
    public void setLocation(String location) {
        this.location = location;
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

    public float getRating() {
        return rating;
    }
    public void setRating(float rating) {
        this.rating = rating;
    }

    public String toJson() {
        return "{" +
                "\"uid\":\"" + uid + "\"," +
                "\"email\":\"" + email + "\"," +
                "\"phoneNumber\":\"" + phoneNumber + "\"," +
                "\"role\":\"" + role + "\"," +
                "\"talentName\":\"" + talentName + "\"," +
                "\"talentDescription\":\"" + talentDescription + "\"," +
                "\"talentCategory\":\"" + talentCategory + "\"," +
                "\"location\":\"" + location + "\"," +
                "\"talentImageUrl\":\"" + talentImageUrl + "\"" +
                "}";
    }
}

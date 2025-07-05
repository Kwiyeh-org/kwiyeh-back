package com.kwiyeh.back.model;

public class UserTalent extends AppUser{
    private String talentName;
    private String talentDescription;
    private String talentCategory;
    private String talentImageUrl;
    private String pricing;
    private String availability;
    private String rating;

    public UserTalent(String uid, String email, String fullName, String phoneNumber, String role,
                      String talentName, String talentDescription, String talentCategory, String talentImageUrl, String location, String pricing, String availability) {
        super(uid, email, fullName, phoneNumber, role);
        this.talentName = talentName;
        this.talentDescription = talentDescription;
        this.talentCategory = talentCategory;
        this.talentImageUrl = talentImageUrl;
        this.pricing = pricing;
        this.availability = availability;
    }

    public UserTalent() {
        // Default constructor for JPA
    }

    public String toJson() {
        return "{" +
                "\"uid\":\"" + getUid() + "\"," +
                "\"email\":\"" + getEmail() + "\"," +
                "\"fullName\":\"" + getFullName() + "\"," +
                "\"phoneNumber\":\"" + getPhoneNumber() + "\"," +
                "\"role\":\"" + getRole() + "\"," +
                "\"talentName\":\"" + talentName + "\"," +
                "\"talentDescription\":\"" + talentDescription + "\"," +
                "\"talentCategory\":\"" + talentCategory + "\"," +
                "\"talentImageUrl\":\"" + talentImageUrl + "\"," +
                "\"pricing\":\"" + pricing + "\"," +
                "\"availability\":\"" + availability + "\"," +
                "\"location\":\"" + (getLocation() != null ? getLocation() : "") + "\"" +
                "}";
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
    public String getRating() {
        return rating;
    }
    public void setRating(String rating) {
        this.rating = rating;
    }
}

package com.kwiyeh.back.model;

public class UserTalent extends AppUser{
    private String talentName;
    private String talentDescription;
    private String talentCategory;
    private String location;
    private String talentImageUrl;

    public UserTalent(String uid, String email, String fullName, String phoneNumber, String type,
                      String talentName, String talentDescription, String talentCategory, String talentImageUrl, String location) {
        super(uid, email, fullName, phoneNumber, type);
        this.talentName = talentName;
        this.talentDescription = talentDescription;
        this.talentCategory = talentCategory;
        this.talentImageUrl = talentImageUrl;
        this.location = location;
    }

    public UserTalent() {
        // Default constructor for JPA
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
}

package com.kwiyeh.back.model;

public class UserClient extends AppUser {
    private String clientName;
    private String clientDescription;
    private String clientCategory;
    private String clientImageUrl;
    private String location;

    public UserClient(String uid, String email, String fullName, String phoneNumber, String type,
                      String clientName, String clientDescription, String clientCategory, String clientImageUrl, String location) {
        super(uid, email, fullName, phoneNumber, type);
        this.clientName = clientName;
        this.clientDescription = clientDescription;
        this.clientCategory = clientCategory;
        this.clientImageUrl = clientImageUrl;
        this.location = location;
    }

    public UserClient() {
        // Default constructor for JPA
    }

    public String getClientName() {
        return clientName;
    }

    public void setClientName(String clientName) {
        this.clientName = clientName;
    }

    public String getClientDescription() {
        return clientDescription;
    }

    public void setClientDescription(String clientDescription) {
        this.clientDescription = clientDescription;
    }

    public String getClientCategory() {
        return clientCategory;
    }

    public void setClientCategory(String clientCategory) {
        this.clientCategory = clientCategory;
    }

    public String getClientImageUrl() {
        return clientImageUrl;
    }

    public void setClientImageUrl(String clientImageUrl) {
        this.clientImageUrl = clientImageUrl;
    }

    public String getLocation() {
        return location;
    }
    public void setLocation(String location) {
        this.location = location;
    }
}

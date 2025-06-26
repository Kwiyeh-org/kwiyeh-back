package com.kwiyeh.back.model;

public class UserClient extends AppUser {
    private String clientImageUrl;

    public UserClient(String uid, String email, String fullName, String phoneNumber, String type,
                      String clientName, String clientDescription, String clientCategory, String clientImageUrl, String location) {
        super(uid, email, fullName, phoneNumber, type);
        this.clientImageUrl = clientImageUrl;
    }

    public UserClient() {
        // Default constructor for JPA
    }

    public String toJson() {
        return "{" +
                "\"uid\":\"" + getUid() + "\"," +
                "\"email\":\"" + getEmail() + "\"," +
                "\"fullName\":\"" + getFullName() + "\"," +
                "\"phoneNumber\":\"" + getPhoneNumber() + "\"," +
                "\"type\":\"" + getType() + "\"," +
                "\"clientImageUrl\":\"" + clientImageUrl + "\"" +
                "}";
    }

    public void setClientImageUrl(String clientImageUrl) {
        this.clientImageUrl = clientImageUrl;
    }
}

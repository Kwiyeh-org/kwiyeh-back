package com.kwiyeh.back.model;

public class UserClient extends AppUser {
    private String clientImageUrl;
    private String location;

    public UserClient(String uid, String email, String fullName, String phoneNumber, String role,
                      String clientName, String clientDescription, String clientCategory, String clientImageUrl, String location) {
        super(uid, email, fullName, phoneNumber, role);
        this.clientImageUrl = clientImageUrl;
        this.location = location;
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
                "\"role\":\"" + getRole() + "\"," +
                "\"clientImageUrl\":\"" + clientImageUrl + "\"," +
                "\"location\":\"" + (location != null ? location : "") + "\"" +
                "}";
    }

    public void setClientImageUrl(String clientImageUrl) {
        this.clientImageUrl = clientImageUrl;
    }

    public String getClientImageUrl() {
        return clientImageUrl;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }
}

package com.example.eventify.models.auth;

public class BusinessOwnerRegistrationRequest extends UserRegistrationRequest {
    private String name;
    private String description;

    public BusinessOwnerRegistrationRequest() {}

    public BusinessOwnerRegistrationRequest(String email, String password, String address, 
                                         String phoneNumber, String name, String description) {
        super(email, password, address, phoneNumber);
        this.name = name;
        this.description = description;
    }

    // Getters and Setters
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}

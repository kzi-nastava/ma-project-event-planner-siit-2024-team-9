package com.example.eventify.models.auth;

public class EventOrganizerRegistrationRequest extends UserRegistrationRequest {
    private String firstName;
    private String lastName;

    public EventOrganizerRegistrationRequest() {}

    public EventOrganizerRegistrationRequest(String email, String password, String address, 
                                          String phoneNumber, String firstName, String lastName) {
        super(email, password, address, phoneNumber);
        this.firstName = firstName;
        this.lastName = lastName;
    }

    // Getters and Setters
    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }
}

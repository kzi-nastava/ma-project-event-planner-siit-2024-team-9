package com.example.eventify.models.others;

import com.example.eventify.models.enums.Status;

public class Invitation {
    private String id;
    private String email;
    private Status status;

    // Default constructor
    public Invitation() {
    }

    // Constructor with parameters
    public Invitation(String id, String email, Status status) {
        this.id = id;
        this.email = email;
        this.status = status;
    }

    // Getters and Setters
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public Status getStatus() {
        return status;
    }

    public void setStatus(Status status) {
        this.status = status;
    }
}

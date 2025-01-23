package com.example.eventify.models.users;

import com.example.eventify.models.enums.UserRole;

import java.util.UUID;

public class Role {
    private UUID id;
    private UserRole name;

    public Role(UserRole name) {
        this.name = name;
    }

    public Role(String role) {
        this.name = UserRole.valueOf(role);
    }

    public Role() {
    }

    public String getAuthority() {
        return name.toString();
    }

    public void setName(UserRole name) {
        this.name = name;
    }

    public UserRole getName() {
        return name;
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    @Override
    public String toString() {
        return name.toString();
    }
}

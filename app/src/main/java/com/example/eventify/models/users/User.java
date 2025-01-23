package com.example.eventify.models.users;


import com.example.eventify.models.enums.UserRole;
import com.example.eventify.models.events.Event;
import com.example.eventify.models.solutions.Solution;

import java.sql.Timestamp;
import java.util.Set;

public class User {
    private String id;
    private String email;
    private String password;
    private Timestamp lastPasswordResetDate;
    private String address;
    private String phoneNumber;
    private String profileImage;
    private Role role;
    private boolean suspended;
    private boolean activated;
    private Set<Solution> favoriteSolutions;
    private Set<Event> attendingEvents;

    public User() {
    }

    public User(String email,
                String password,
                String address,
                String phoneNumber,
                String profileImage) {
        this.email = email;
        this.password = password;
        this.lastPasswordResetDate = new Timestamp(System.currentTimeMillis());
        this.address = address;
        this.phoneNumber = phoneNumber;
        this.role = new Role(UserRole.AUTHENTICATED_USER);
        this.profileImage = profileImage;
        this.suspended = false;
        this.activated = false;
    }

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

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public Timestamp getLastPasswordResetDate() {
        return lastPasswordResetDate;
    }

    public void setLastPasswordResetDate(Timestamp lastPasswordResetDate) {
        this.lastPasswordResetDate = lastPasswordResetDate;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public String getProfileImage() {
        return profileImage;
    }

    public void setProfileImage(String profileImage) {
        this.profileImage = profileImage;
    }

    public Role getRole() {
        return role;
    }

    public void setRole(Role role) {
        this.role = role;
    }

    public boolean isSuspended() {
        return suspended;
    }

    public void setSuspended(boolean suspended) {
        this.suspended = suspended;
    }

    public boolean isActivated() {
        return activated;
    }

    public void setActivated(boolean activated) {
        this.activated = activated;
    }

    public Set<Solution> getFavoriteSolutions() {
        return favoriteSolutions;
    }

    public void setFavoriteSolutions(Set<Solution> favoriteSolutions) {
        this.favoriteSolutions = favoriteSolutions;
    }

    public Set<Event> getAttendingEvents() {
        return attendingEvents;
    }

    public void setAttendingEvents(Set<Event> attendingEvents) {
        this.attendingEvents = attendingEvents;
    }
}

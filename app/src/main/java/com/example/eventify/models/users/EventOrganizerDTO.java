package com.example.eventify.models.users;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.UUID;

public class EventOrganizerDTO {
    @SerializedName("id")
    @Expose
    private String id;
    
    @SerializedName("email")
    @Expose
    private String email;
    
    @SerializedName("password")
    @Expose
    private String password;
    
    @SerializedName("address")
    @Expose
    private String address;
    
    @SerializedName("phoneNumber")
    @Expose
    private String phoneNumber;
    
    @SerializedName("profileImage")
    @Expose
    private String profileImage;
    
    @SerializedName("role")
    @Expose
    private RoleDTO role;
    
    // Add a direct role name field for polymorphic deserialization
    @SerializedName("role.name")
    @Expose
    private String roleName;
    
    @SerializedName("firstName")
    @Expose
    private String firstName;
    
    @SerializedName("lastName")
    @Expose
    private String lastName;

    public EventOrganizerDTO() {
    }

    public EventOrganizerDTO(String id, String email, String password, String address, String phoneNumber, String profileImage, RoleDTO role, String firstName, String lastName) {
        this.id = id;
        this.email = email;
        this.password = password;
        this.address = address;
        this.phoneNumber = phoneNumber;
        this.profileImage = profileImage;
        this.role = role;
        this.firstName = firstName;
        this.lastName = lastName;
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

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
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

    public RoleDTO getRole() {
        return role;
    }

    public void setRole(RoleDTO role) {
        this.role = role;
    }

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

    public String getRoleName() {
        return roleName;
    }

    public void setRoleName(String roleName) {
        this.roleName = roleName;
    }
}
